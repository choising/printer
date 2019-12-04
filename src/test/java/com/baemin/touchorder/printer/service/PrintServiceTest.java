package com.baemin.touchorder.printer.service;

import com.baemin.touchorder.printer.dto.PrintItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author seungmin
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrintServiceTest {

    @Autowired
    PrintService printService;

    @Test
    public void testPrint() {

        PrintItem item = PrintItem.builder()
                .shopNumber(123456789)
                .shopName("최승민테스트테스트테스트테스트...")
                .qrType("서빙 QR")
                .tableNumber("table 1")
                .tableName("테이블 1")
                .token("616D35EXEQXMEMVRC615191120")
                .qrImageUrl("https://cf-simple-s3-origin-touch-order-prod-contents-760831942475.s3.ap-northeast-2.amazonaws.com/qrcode/13029682/qr-13029682-0-20190903140444.png")
                .build();

        printService.testPrint(item);
    }

}
