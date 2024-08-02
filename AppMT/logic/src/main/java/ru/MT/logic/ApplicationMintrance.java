package ru.MT.logic;

import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.*;
import ru.MT.logic.ConfigManager;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.POIXMLException;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
//import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;


public class ApplicationMintrance {
    private static double prevResult = 0;
    private static final String CONFIG_FILE_NAME = "src/main/resuorces/config.properties";
    private static boolean inWork = true; //Переменная определяющая работу программы
    private static String apiKey;
    private static String mode;
    private static Boolean avoidTolls;
    private static File executibleFile; // Здесь храним файл выбранный пользователем
    private static int rowToStart =10; //     В переменной хранится значение индекса строки с которой необходимо начать
    private static int generalColumns; // Здесь храним индекс последней колонки + 1
    private static int generalRows; // Здесь храним число строк оптимальных для выполнения 1000 запросов в день, зависит от числа столбцов
    private static ConfigManager configManager;

    private static ProgressUpdateListener progressUpdateListener;

    public void setProgressUpdateListener(ProgressUpdateListener listener) {
        this.progressUpdateListener = listener;
    }

    public static void appMT(File file) throws IOException, ColsMoreHundredException, UnauthorisedException, HTTPForbiddenException, TooManyRequestsException {

        configManager= new ConfigManager();

        apiKey = configManager.getKey();
        mode = configManager.getMode();
        avoidTolls = configManager.getAvoidTolls();

        Scanner scanner = new Scanner(System.in);

        ArrayList<JsonObject> resultArray = new ArrayList<>();


        ArrayList<String> requestArray = new ArrayList<String>();

            /*System.out.println("Убедитесь, что в выбранном файле структура соответствует следующим параметрам:\n" + // TODO: Переписать под новую структуру
                    "В столбце \"F\"  начиная с 11-й строки перечислен координаты северной широты,\n" +
                    "а в столбце \"G\" c 11-й строки перечислены координаты западной долготы точки отправления.\n\n" +
                    "Так же в столбцах, начиная с \"J\", в 8-й строке указана северная широта, \n" +
                    "а в 9й строке того же столбца  указана восточная долгота точки назначения\n\n" +
                    "Помните, что количество столбцов характеризующих точки назначения не должно превышать 100 шт." +
                    "\nМежду ними не должно быть пробелов.");*/
        try {
            requestArray = fileExecuting(file);
        } catch (ColsMoreHundredException e){
            System.out.println("\n К сожалению в вашем файле существует более 100 пунктов назначения, пожалуйста сократите количество");
            throw new ColsMoreHundredException();
        }catch (NullPointerException e) {
            System.out.println("\nФайл Пуст. Проверьте на содержание\n\n" + e);
            throw new NullPointerException();
        } catch (POIXMLException e) {
            System.out.println("\nНеверный формат входного файла, выберите .xlsx " + e);
            throw new NullPointerException();
        } catch (IllegalArgumentException e){
            System.out.println("\nВ выбранном файле отсутвует 2й лист");
            throw new IllegalArgumentException();
        }catch (FileNotFoundException e){
            throw new FileNotFoundException();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nВозникла непредвиденная ошибка\n" + e);
            return;
        }
        if(requestArray.size()==0){
           //continue;
        }

        try {
            resultArray = requestData(requestArray,resultArray);//Идем по сформированному request одной строки
        } catch(UnknownHostException e){
            System.out.println("Не удалось подключиться к сервису Yandex. Проверьте Интернет - соединение");
            throw new UnknownHostException();
        } catch (UnauthorisedException e){
            System.out.println("\n\"Ошибка валидации ключа доступа. Проверьте текущий статус ключа, при необходимоти - смените\"");
            throw new UnauthorisedException();
        } catch (HTTPForbiddenException e){
            System.out.println("\n!!! Внимание !!! Лимит запросов на сегодня превышен!");
            throw new HTTPForbiddenException();
        } catch (TooManyRequestsException e){
            System.out.println("\n!!! Внимание !!! Лимит запросов на сегодня превышен!/ Слишком много запросов");
            throw new TooManyRequestsException();
        }


        WriteInExcel(resultArray);


        scanner.close();
    }

    public static void SetNewApiKey(String newKey){
        apiKey = newKey;
        System.out.println("Новый ключ: " + '"' + apiKey + '"' + " - был успешно задан");
        //System.out.println("\nНажмите на любую клавишу - затем Enter, чтобы вернуться в Меню");

    }
    public static ArrayList<JsonObject> requestData(ArrayList<String> requestArray,ArrayList<JsonObject> resultArray) throws UnknownHostException, UnauthorisedException, HTTPForbiddenException, TooManyRequestsException {
        HttpURLConnection connection = null;
        StringBuilder response;
        JsonObject json = new JsonObject();
        for (int i =0;i<requestArray.size();i++) { //TODO:23.03.2025  Здесь должно быть либо статическое значение, либо меняем ширину в динамике
            try {
                URL url = new URL(requestArray.get(i));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);

                connection.connect();

                drawProgressBar(i + 1, requestArray.size());
                double progress =((double) (i + 1) / requestArray.size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (progressUpdateListener != null) {
                    progressUpdateListener.updateProgressBar(progress);
                }

                StringBuilder responseBuilder = new StringBuilder();
                //int test = connection.getResponseCode();
                if (401 == connection.getResponseCode()) {
                    if (resultArray.size()==0){
                        throw new UnauthorisedException("");
                    }
                    else{
                        return resultArray;
                    }
                } else if (200 == connection.getResponseCode()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    in.close();
                    String jsonResponse = responseBuilder.toString();

                    Gson gson = new Gson();
                    json = gson.fromJson(jsonResponse, JsonObject.class);

                }
                else if (403 == connection.getResponseCode()) {
                    System.out.println("\n!!! Внимание !!! Лимит запросов на сегодня превышен!");
                    return resultArray;
                }
                else if(429==connection.getResponseCode()){
                    System.out.println("\n Слишком много запросов в секунду");
                    return resultArray;
                }

                connection.disconnect();
            } catch (UnknownHostException e) {
                throw new UnknownHostException();
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (UnauthorisedException e) {
                throw new UnauthorisedException("");
            }  finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            resultArray.add(json);
        }
        return resultArray;
    }

    public static ArrayList<String> fileExecuting(File file) throws POIXMLException, ColsMoreHundredException, FileNotFoundException {

        ArrayList<String> requestBodyList = new ArrayList<String>();

        if (file != null) {
            executibleFile = file;
            try {
                FileInputStream inputStreamChecker = new FileInputStream(executibleFile);
                Workbook workbookChecker = new XSSFWorkbook(inputStreamChecker);
                FileOutputStream fileOut = new FileOutputStream(executibleFile);
                workbookChecker.write(fileOut);
                workbookChecker.close();
                fileOut.close();
                FileInputStream inputStream = new FileInputStream(executibleFile);
                Workbook book = new XSSFWorkbook(inputStream);
                Sheet sheet = book.getSheetAt(0); //TODO: Добавить второй лист.
                Sheet sheetChecker = book.getSheetAt(1);
                DetectionOfLastRow(sheet);
                try {
                    CountingGeneralCols(sheet);
                } catch (ColsMoreHundredException e) {
                    throw new ColsMoreHundredException("Колонок больше 100");
                }
                FindCountofExecutibleRows(sheet);

                for (int rowOrig = rowToStart;rowOrig<=rowToStart+generalRows-1;rowOrig++){ // TODO: Поправить в циклах со статикой перед сдачей.

                    StringBuilder requestBody = new StringBuilder();

                    Row curRowOrigin = sheet.getRow(rowOrig);
                    String latOrigin = curRowOrigin.getCell(5).toString().replaceAll("\\u00A0", "").trim();
                    String lonOrigin = curRowOrigin.getCell(6).toString().replaceAll("\\u00A0", "").trim();

                    requestBody.append("https://api.routing.yandex.net/v2/distancematrix?origins="+latOrigin+","+lonOrigin+"&destinations=");

                    for(int j = 9;j<generalColumns;j++){ //TODO: 23.03.2024
                        Row curRowDestinationLat = sheet.getRow(7);
                        Row curRowDestinationLon = sheet.getRow(8);
                        String latDest = curRowDestinationLat.getCell(j).toString().replaceAll("\\u00A0", "").trim();
                        String lonDest = curRowDestinationLon.getCell(j).toString().replaceAll("\\u00A0", "").trim();

                        requestBody.append(latDest+","+lonDest+"|");
                    }

                    requestBody.deleteCharAt(requestBody.length() - 1);
                    requestBody.append("&mode="+mode+"&avoid_tolls="+avoidTolls+"&apikey="+apiKey);

                    requestBodyList.add(requestBody.toString());
                }


                inputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("\n\nВыбранный файл открыт, закройте и повторите попытку");
                throw new FileNotFoundException();
            }catch (IOException e) {
                e.printStackTrace();
            }catch (OutOfMemoryError e) {
                System.out.println("Файл слишком велик, выберите меньший по размеру файл");
            }catch (IllegalArgumentException e){
                throw new IllegalArgumentException();
            }
        }
        return requestBodyList;
    }
    public static void CountingGeneralCols(Sheet sheet) throws ColsMoreHundredException {
        Row row = sheet.getRow(3);//Получаем строку по которой будем считать кол-во столбцов
        boolean rowEnded =false;
        int cellIndex = 9;
        int countCols = 0;

        while(!rowEnded){
            Cell cell = row.getCell(cellIndex);
            if(cell == null|| cell.toString().replaceAll("\\u00A0", "").trim().isBlank()==true){
                break;
            }
            cellIndex++;
            countCols++;
            if(countCols>100){
                throw new ColsMoreHundredException("Ошибка");
            }
        }
        generalColumns = cellIndex;
        CountingRows(countCols);

    }

    public static void CountingRows(int countCols){
        int limitOfDayRequest = 10000;
        int res;
        res = limitOfDayRequest/countCols;
        generalRows = res;
    }
    public static void  FindCountofExecutibleRows(Sheet sheet){
        int emptyRowCount = 0;
        int localI = rowToStart;
        while (true){
            Row row = sheet.getRow(localI);
            if(row == null){
                break;
            }
            Cell cell = row.getCell(5); // По структуре ищем по колонке с северной широтой Структура
            if(cell ==null||cell.toString().replaceAll("\\u00A0", "").trim().isBlank() == true){
                break;
            }else {
                localI++;
                emptyRowCount++;
            }
        }
        if (generalRows >emptyRowCount){
            generalRows =emptyRowCount;
        }
    }

    public static void DetectionOfLastRow(Sheet sheet) throws IOException {


        int startRow = 10; // Начальная строка диапазона
        int endRow = sheet.getLastRowNum(); // Конечная строка диапазона
        int columnToCheck = 9;
        int rowCounter = 0;

        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(columnToCheck);
            if (cell==null||cell.toString().replaceAll("\\u00A0", "").trim().isBlank()==true) {
                rowToStart = i;
                break;
            }
            rowToStart = i;
        }
    }
    public static void WriteInExcel(ArrayList<JsonObject> arrayResult) throws IOException {
        File execFile = executibleFile;
        FileInputStream inputStream = new FileInputStream(execFile);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheetMinutes = workbook.getSheetAt(0);
        Sheet sheetKilometers = workbook.getSheetAt(1); //TODO: Отдельный метод валидации файла
        Cell modeCell = sheetMinutes.getRow(8).createCell(7, 0);
        modeCell.setCellValue("Mode: "+ mode.toString());
        Font fontMode = workbook.createFont();
        fontMode.setBold(true);
        fontMode.setFontName("Calibri");
        fontMode.setFontHeightInPoints((short)16);
        CellStyle styleMode = workbook.createCellStyle();
        styleMode.setFont(fontMode);
        modeCell.setCellStyle(styleMode);


        for (int rowIndex = rowToStart, i = 0; i<arrayResult.size(); i++, rowIndex++) { //TODO:23.03.2024  Здесь должно быть либо статическое значение, либо меняем ширину в динамике
            JsonObject singleResponse = arrayResult.get(i);
            JsonArray rows = singleResponse.getAsJsonArray("rows");
            if (rows == null){
                continue;
            }
            JsonObject rowElement = rows.get(0).getAsJsonObject();
            JsonArray elements = rowElement.getAsJsonObject().getAsJsonArray("elements");

            Row rowMin = sheetMinutes.getRow(rowIndex);
            Row rowKms = sheetKilometers.getRow(rowIndex);

            for(int col = 9, j=0; col < generalColumns;j++, col++){ //TODO:23.03.2024 Здесь должно быть либо статическое значение, либо меняем ширину в динамике
                JsonObject currentElement = elements.get(j).getAsJsonObject();
                String status = currentElement.get("status").getAsString();
                if ("FAIL".equals(status)){
                    Cell cellMin = rowMin.createCell(col);
                    Cell cellKms = rowKms.createCell(col);
                    cellMin.setCellValue(status);
                    cellKms.setCellValue(status);
                }
                else{
                    JsonObject durationObject = currentElement.getAsJsonObject("duration");
                    JsonObject distanceObject = currentElement.getAsJsonObject("distance");

                    DecimalFormat df = new DecimalFormat("#.##"); // Определяем формат с двумя знаками после запятой

                    double durationValue = durationObject.get("value").getAsDouble() / 60; //Получаем минуты

                    double distanceValue = distanceObject.get("value").getAsDouble() / 1000; // Получаем километры

                    Cell cellMin = rowMin.createCell(col);
                    Cell cellKms = rowKms.createCell(col);

                    cellMin.setCellValue(df.format(durationValue));
                    cellKms.setCellValue(df.format(distanceValue));
                }

            }
        }


        Scanner scanner = new Scanner(System.in);
        boolean successfulWrite = false;

        do {
            try {
                FileOutputStream fileOut = new FileOutputStream(execFile);
                workbook.write(fileOut);
                workbook.close();
                fileOut.close();
                successfulWrite = true; // Запись прошла успешно, выходим из цикла
            } catch (FileNotFoundException e) {
                System.out.println("\n\nВозникла ошибка при записи в файл: " + e.getMessage() +
                        "\nПроверьте, что файл закрыт на устройстве - в Excel");
                System.out.println("Повторить? - в противном случае данные будут потеряны (Y/N):");
                String response = scanner.nextLine().toUpperCase();
                if (!response.equals("Y")) {
                    System.out.println("\n Данные были стерты, придется повторить запросы");
                    break; // Прерываем цикл, если пользователь не хочет повторить попытку
                }
            }
        } while (!successfulWrite);
        if (successfulWrite) {
            System.out.println("\nДанные были успешно записаны. " +
                    "\nПервая страница файла - значения в минутах, вторая - в километрах");
        }
    }
    public static void ExitFromApp(){
        // Создаем и запускаем поток, который будет выводить точки
        Thread dotsThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print(".");
                    Thread.sleep(300); // Пауза между точками (в миллисекундах)
                    System.out.print("\b\b\b"); // Удаляем точки, чтобы она заменялась следующей
                    Thread.sleep(500); // Пауза перед следующей точкой (в миллисекундах)
                }
            } catch (InterruptedException e) {
                // Обработка прерывания потока
            }
        });
        dotsThread.start(); // Запускаем поток с точками

        try {
            // Пауза перед остановкой потока с точками
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }
    private static void loadSettings() {
        Properties prop = new Properties();
        File configFile = new File(System.getProperty("user.home"), "config.properties");

        try {
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    prop.load(input);
                }
            } else {
                // Load default settings from JAR file
                try (InputStream input = ApplicationMintrance.class.getClassLoader().getResourceAsStream("config.properties")) {
                    if (input != null) {
                        prop.load(input);
                    } else {
                        System.err.println("Default config file not found in JAR.");
                        return;
                    }
                }
            }

            // Retrieve the apiKey setting
            apiKey = prop.getProperty("apiKey");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveSettings() {
        Properties prop = new Properties();
        prop.setProperty("apiKey", apiKey);
        File configFile = new File(System.getProperty("user.home"), "config.properties");
        try (OutputStream output = new FileOutputStream(configFile)) {
            prop.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    // Метод для рисования прогресс-бара
    private static void drawProgressBar(int currentRequest, int totalRequests) {
        int progress = (currentRequest * 100) / totalRequests;
        int progressBarLength = 50;
        int numFilled = (progress * progressBarLength) / 100;
        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < progressBarLength; i++) {
            if (i < numFilled) {
                progressBar.append("=");

            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ").append(progress).append("%");
        System.out.print("\r" + progressBar.toString());
    }

}
