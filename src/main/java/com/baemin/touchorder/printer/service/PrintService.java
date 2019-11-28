package com.baemin.touchorder.printer.service;

import com.baemin.touchorder.printer.dto.PrintDto;
import com.baemin.touchorder.printer.dto.PrintItem;
import com.baemin.touchorder.printer.util.ImageUrlToZplConverter;
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
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PrintService implements PrintProvider{
    private Connection connection;
    private ZebraPrinter printer;
    private ImageUrlToZplConverter converter = new ImageUrlToZplConverter();

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

    private byte[] getZPL(PrintItem printItem) throws IOException {

        String document = "^XA^CW2,E:BMHANNA_11YRS_TT.TTF^CI26^FS";
        document = getConditionZPL(printItem, document);

        return document.getBytes();
    }

    private String getConditionZPL(PrintItem printItem, String document) throws IOException {

        document += String.format("^FO24,24^A2N,30,30^FD%s^FS", printItem.getShopName());
        document += String.format("^FO24,64^A2N,30,30^FD%s^FS", printItem.getQrType());

        String tableNumber = printItem.getTableNumber();

        if (!tableNumber.equals("0") && !tableNumber.equals("99999")) {
            document += String.format("^FO24,104^A2N,30,30^FD%s^FS", printItem.getTableNumber());
            document += String.format("^FO24,144^A2N,30,30^FD%s^FS", printItem.getTableName());
        }

        document += String.format("^FO24,184^A2N,30,30^FD%s^FS", printItem.getToken());
        document += converter.convertFromImg(getBufferedImage(printItem.getQrImageUrl()), 24, 210);

        if (!tableNumber.equals("0") && !tableNumber.equals("99999")) {
            document += String.format("^FO24,450^A2N,30,30^FD%s^FS^XZ", printItem.getTableName());
        }

        return document;
    }

    @Async
    @Override
    public List<String> print(PrintDto printDto) {

        List<PrintItem> printItems = printDto.getPrintItems();
        int count = printDto.getCount();
        List<String> failList = new ArrayList<>();

        for (PrintItem printItem : printItems) {
            for (int i = 0; i < count; i++) {
                try {
                    connection.write(getZPL(printItem));
                } catch (ConnectionException e) {
                    failList.add(printItem.getToken());
                    log.error("Label Connection Error, errorMessage: {}, failList: {}", e.getMessage(), failList, e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

            BufferedImage img = getBufferedImage("https://cf-simple-s3-origin-touch-order-prod-contents-760831942475.s3.ap-northeast-2.amazonaws.com/qrcode/13029682/qr-13029682-0-20190903140444.png");

            String document = "^XA^CW2,E:BMHANNA_11YRS_TT.TTF^CI26^FS";
            document += String.format("^FO24,24^A2N,30,30^FD%s^FS", shopName);
            document += String.format("^FO24,64^A2N,30,30^FD%s^FS", qrType);
            document += String.format("^FO24,104^A2N,30,30^FD%s^FS", tableNumber);
            document += String.format("^FO24,144^A2N,30,30^FD%s^FS", tableName);
            document += String.format("^FO24,184^A2N,30,30^FD%s^FS", token);
            document += converter.convertFromImg(img, 24, 210);
            document += String.format("^FO24,650^A2N,30,30^FD%s^FS^XZ", tableName);

            connection.write(document.getBytes());
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage getBufferedImage(String urlText) throws IOException {
        URL url = new URL(urlText);
        BufferedImage originalImage = ImageIO.read(url);

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        System.out.println("w : " + w +", h : " + h);
        BufferedImage resizeImage = new BufferedImage(w / 16, h / 16, originalImage.getType());
        Graphics2D g = resizeImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, w / 16, h / 16, 0, 0, w, h, null);
        g.dispose();
        System.out.println("resize w : " + w +", resize h : " + h);
        return resizeImage;
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
