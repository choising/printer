package com.baemin.touchorder.printer.service;

import com.baemin.touchorder.printer.dto.PrintDto;
import com.baemin.touchorder.printer.dto.PrintItem;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.DriverPrinterConnection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterDriver;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PrintService implements PrintProvider{
    private Connection connection;
    private ZebraPrinter printer;

    @PostConstruct
    public void postConstruct() {
        try {
            DiscoveredPrinterDriver[] discoveredPrinterDrivers = UsbDiscoverer.getZebraDriverPrinters();
            if (discoveredPrinterDrivers.length == 0) {
                log.error("Not Found Printer");
                return;
            }
            connection = new DriverPrinterConnection(discoveredPrinterDrivers[0].printerName);
            connection.open();
            log.info("Connection Open");
        } catch (ConnectionException e) {
            log.error("Print Connection Fail!!, errorMessage: {}", e.getMessage(), e);
            System.exit(-1);
        }
        getPrinter();
    }

    private void getPrinter() {
        try {
            long start = System.currentTimeMillis();

            printer = ZebraPrinterFactory.getInstance(connection);

            long timeElapsed = System.currentTimeMillis() - start;
            log.info("Printer Is Ready, elapsed Time: {}ms", timeElapsed);
        } catch (ConnectionException e) {
            log.error("Print Connection Fail!!, errorMessage: {}", e.getMessage(), e);
        } catch (ZebraPrinterLanguageUnknownException e) {
            log.error("Zebra Printer Language Unknown, errorMessage: {}", e.getMessage(), e);
        }
    }

    private byte[] getZPL(PrintItem printItem) {
        String assetId = printItem.getAssetId();
        String assetName = printItem.getAssetName();
        String userDepartmentName1 = printItem.getUserDepartmentName().isEmpty() ? "재고" : printItem.getUserDepartmentName();
        String userDepartmentName2 = "";

        if (printItem.getUserDepartmentName().length() > 10) {
            userDepartmentName1 = printItem.getUserDepartmentName().substring(0,9);
            userDepartmentName2 = printItem.getUserDepartmentName().substring(10);
        }

        String userInfo = printItem.getUserName().isEmpty() ? "" : printItem.getUserName() + "(" + printItem.getUserNumber() + ")";
        String serialNumber = printItem.getSerialNumber();
        if (serialNumber.length() > 13){
            serialNumber = serialNumber.substring(0, 13);
        }

        return String.format("^XA^LL648^CW1,E:KFONT3.FNT^CI28^FS^FO24,24^A1N,30,30^FD%s^FS^FO24,64^A1N,30,30^FD%s^FS^FO24,104^A1N,30,30^FD%s^FS^FO24,144^A1N,30,30^FD%s^FS^FO24,184^A1N,30,30^FD%s^FS^FO360,295^BQ,2,5^FDHM,A%s^FS^FO24,550^A1N,30,30^FD%s^FS^XZ",
                userDepartmentName1,
                userDepartmentName2,
                userInfo,
                assetId,
                serialNumber,
                assetId,
                assetName).getBytes();
    }

    // TODO 유효성 체크
    @Async
    @Override
    public List<String> print(PrintDto printDto) {
        List<PrintItem> printItems = printDto.getSortedList();
        List<String> failList = new ArrayList<String>();
        for (PrintItem printItem : printItems) {
            String assetId = printItem.getAssetId();
            try {
                if (invalidCheck(assetId)) {
                    failList.add(assetId);
                    continue;
                }
                connection.write(getZPL(printItem));
            } catch (ConnectionException e) {
                failList.add(assetId);
                log.error("Label Connection Error, errorMessage: {}", e.getMessage(), e);
            }
        }

        try {
            long millis = printItems.size() * 1100;
            Thread.sleep(millis);
            cutting();
        } catch (InterruptedException e) {
            log.error("Sleep Interrupted Error, errorMessage: {}", e.getMessage(), e);
        }

        return failList;
    }

    @Override
    public void print() {
        try {
            String shopName = "배민카페";
            String qrType = "서빙QR";
            String tableNumber = "table 1";
            String tableName = "일이삼사오육칠팔구십";
            String token = "328321983218321983218";
            String qrImage = "hello world";

            String document = "";
            document += "^XA";
            document += "^CW1,E:KFONT3.TTF^CI28^FS";
            document += String.format("^FO24,24^A1N,30,30^FD%s^FS", shopName);
            document += String.format("^FO24,64^A1N,30,30^FD%s^FS", qrType);
            document += String.format("^FO24,104^A1N,30,30^FD%s^FS", tableNumber);
            document += String.format("^FO24,144^A1N,30,30^FD%s^FS", tableName);
            document += String.format("^FO24,184^A1N,30,30^FD%s^FS", token);
            document += String.format("^FO24,295^BQ,2,5^FDHM,A%s^FS", qrImage);
            document += String.format("^FO24,550^A1N,30,30^FD%s^FS", tableName);
            document += "^XZ";

            connection.write(document.getBytes());
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cutting() {
        try {
            connection.write("^XA~JK^XZ".getBytes());
        } catch (ConnectionException e) {
            log.error("Cutting Connection Error, errorMessage: {}", e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean isReady() {
        if (printer == null) {
            getPrinter();
            return false;
        }
        return true; // printer.getCurrentStatus().isReadyToPrint;
    }

    @Override
    public void reconnection() {
        if (printer == null) {
            getPrinter();
            log.info("reconnection print driver");
        }
    }

    private boolean invalidCheck(String assetId) {

        if (assetId.isEmpty()) {
            return true;
        }

        // ex MB19050001 pattern match
        if (!Pattern.matches("[A-Z]{2}[0-9]{8}", assetId)) {
            return true;
        }

        return false;
    }

    @PreDestroy
    public void destroy() {
        try {
            connection.close();
        } catch (ConnectionException e) {
            log.error("Close Connection Error, errorMessage: {}", e.getMessage(), e);
        }
    }
}
