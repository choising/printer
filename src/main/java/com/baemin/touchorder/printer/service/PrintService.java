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
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class PrintService {

    private static final String FONT_HANNA_PRO = "E:BMHANNAPRO.TTF";

    private static final String FONT_EULJIRO = "E:BMEULJIROTTF.TTF";

    private Connection connection;

    private ZebraPrinter printer;

    private ImageUrlToZplConverter converter = new ImageUrlToZplConverter();

    @PostConstruct
    public void postConstruct() {
        try {
            // find usb driver printers
            DiscoveredPrinterDriver[] discoveredPrinterDrivers = UsbDiscoverer.getZebraDriverPrinters();
            if (discoveredPrinterDrivers.length == 0) {
                log.error("Not Found Printer");
                return;
            }

            // get first index printer
            connection = new DriverPrinterConnection(discoveredPrinterDrivers[0].printerName);

            // open
            connection.open();

            log.info("Connection Open");
        } catch (ConnectionException e) {
            log.error("Print Connection Fail!!, errorMessage: {}", e.getMessage(), e);
            System.exit(-1);
        }

        setPrinter();
    }

    @Synchronized
    public List<String> print(PrintDto printDto) {

        // print fail list
        List<String> failList = new ArrayList<>();

        for (PrintItem printItem : printDto.getPrintItems()) {
            for (int i = 0; i < printDto.getCount(); i++) {
                try {
                    log.info("[Print-Start] lets print! - token : {}", printItem.getToken());
                    connection.write(getZPL(printItem));
                } catch (ConnectionException e) {
                    failList.add(printItem.getToken());
                    log.error("[Print-Fail] Label Connection Error, errorMessage: {}, fail: {}", e.getMessage(), printItem.getToken(), e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return failList;
    }

    public void testPrint(PrintItem item) {
        try {
            String document = getHeaderOfZPL(FONT_HANNA_PRO);

            document += String.format("^FO05,15^A2N,30,30^FD%s^FS", item.getShopNumber());
            document += String.format("^FO05,55^A2N,30,30^FD%s^FS", item.getShopName());
            document += String.format("^FO05,95^A2N,30,30^FD%s^FS", item.getQrType());
            document += String.format("^FO05,135^A2N,30,30^FD%s^FS", item.getTableNumber());
            document += String.format("^FO05,175^A2N,30,30^FD%s^FS", item.getTableName());
            document += String.format("^FO05,210^A2N,25,25^FD%s^FS", item.getToken());
            if (isServingQr(item.getTableNumber())) {
                document += converter.convertFromImg(getBufferedImage(item.getQrImageUrl()), 58, 250);
                document += String.format("^FO0,535^A2N,30,30^FB430,1,0,C^FD%s^FS^XZ", item.getTableName());
            } else {
                document += converter.convertFromImg(getBufferedImage(item.getQrImageUrl()), 58, 265);
            }
            document += "^XZ";

            connection.write(document.getBytes());
            System.out.println(document);

        } catch (ConnectionException | IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getZPL(PrintItem item) throws IOException {

        String document = getHeaderOfZPL(FONT_HANNA_PRO);

        document += String.format("^FO05,15^A2N,30,30^FD%s^FS", item.getShopNumber());
        document += String.format("^FO05,55^A2N,30,30^FD%s^FS", item.getShopName());
        document += String.format("^FO05,95^A2N,30,30^FD%s^FS", item.getQrType());

        String tableNumber = item.getTableNumber();

        // 서빙 QR 일 때 추가 정보, tableNumber 를 기준으로 0과 99999가 아니면!
        if (isServingQr(tableNumber)) {
            document += String.format("^FO05,135^A2N,30,30^FD%s^FS", tableNumber);
            document += String.format("^FO05,175^A2N,30,30^FD%s^FS", item.getTableName());
        }

        document += String.format("^FO05,210^A2N,25,25^FD%s^FS", item.getToken());

        // 서빙 QR 일 때 QR 아래에 테이블 명을 적어준다.
        if (isServingQr(tableNumber)) {
            document += converter.convertFromImg(getBufferedImage(item.getQrImageUrl()), 58, 250);
            document += String.format("^FO0,535^A2N,30,30^FB430,1,0,C^FD%s^FS", item.getTableName());
        } else {
            // 픽업, 미리보기 QR 일 때에는 아래 공간을 조금 덜 확보하고 중앙에 맞추기 위하여 Y 좌표를 조금 더 내린다.
            document += converter.convertFromImg(getBufferedImage(item.getQrImageUrl()), 58, 265);
        }

        document += "^XZ";

        log.info("[Print-Make-ZPL] Success - zpl: {}", document);

        return document.getBytes();
    }

    private void setPrinter() {
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

    private boolean isServingQr(String tableNumber) {
        return !tableNumber.equals("0") && !tableNumber.equals("99999");
    }

    private String getHeaderOfZPL(String font) {
        return String.format("^XA^CW2,%s^CI28^FS", font);
    }

    private BufferedImage getBufferedImage(String urlText) throws IOException {
        URL url = new URL(urlText);
        BufferedImage originalImage = ImageIO.read(url);

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        // qr size 300 x 300
        BufferedImage resizeImage = new BufferedImage(300, 300, originalImage.getType());
        Graphics2D g = resizeImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, 300, 300, 0, 0, w, h, null);
        g.dispose();
        return resizeImage;
    }

    public void cutting() {
        try {
            connection.write("^XA~JK^XZ".getBytes());
        } catch (ConnectionException e) {
            log.error("Cutting Connection Error, errorMessage: {}", e.getMessage(), e);
        }
    }

    public synchronized boolean isReady() {
        if (printer == null) {
            setPrinter();
            return false;
        }
        return true;
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
