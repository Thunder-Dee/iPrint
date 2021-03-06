package com.iuv.print;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.sf.dto.WaybillDto;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sun.print.PageableDoc;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author thunder.dee.yi@gmail.com
 */
@Slf4j
@CrossOrigin
@Controller
@SpringBootApplication
public class PrintApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintApplication.class, args);
    }

    //*****************************/?????????/*****************************//
    //    ???????????????????????????WebSocket???
    //    ??????????????????Pdf-BASE64???????????????
    //    ????????????????????????????????????????????????????????????
    //    ??????????????????????????????????????????????????????????????????(??????)
    //    ?????????
    //    ??????????????????WebSocket???????????????????????????????????????????????????????????????Nginx?????????????????????????????????
    //*****************************/?????????/*****************************//

    @ResponseBody
    @GetMapping("printer")
    public String searchDefaultPrinter() {
        return PrintServiceLookup.lookupDefaultPrintService().getName();
    }

    /**
     * ????????????????????????
     *
     * @param base64 PDF
     */
    @PostMapping("/print/pdf")
    public void handleBase64(@RequestParam String base64) {
        log.info("??????????????????:{}", base64);
        print(Base64.decode(base64));
        log.info("??????????????????");
    }

    /**
     * ????????????Pdf???????????? <br>
     * ?????????Windows???????????????PDF???????????????
     *
     * @param bytes
     */
    @SneakyThrows
    public static void print(byte[] bytes) {
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        DocPrintJob printJob = printService.createPrintJob();
        try (PDDocument load = PDDocument.load(bytes)) {
            PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
            attributeSet.add(OrientationRequested.PORTRAIT);
            attributeSet.add(MediaSizeName.INVOICE);
            attributeSet.add(new MediaSize(1000, 2100, Size2DSyntax.MM));
//            ????????????PDF
            printJob.print(new PageableDoc(new PDFPageable(load)), attributeSet);

//            1.4??????????????????
            PDFPrintable printable = new PDFPrintable(load, Scaling.ACTUAL_SIZE);
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(printable);
            job.defaultPage();
            job.print();

//            ???????????????
            HashDocAttributeSet hashDocAttributeSet = new HashDocAttributeSet();
            PDFRenderer pdfRenderer = new PDFRenderer(load);
            for (int i = 0; i < load.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, 256);
//                ??????PDF???????????????
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                File file = new File("D:\\print\\" + LocalDateTime.now() + i + ".png");
                ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
//                ??????PNG??????
                DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
                ByteArrayInputStream input = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                Doc doc = new SimpleDoc(input, flavor, hashDocAttributeSet);
                printJob.print(doc, attributeSet);
            }
        }
    }

    //*****************************/?????????/*****************************//
    //    TODO???
    //    ??????????????????WebSocket Client???????????????
    //    ??????????????????????????????????????????????????????SSID????????????????????????(???????????????????????????????????????????????????????????????)
    //    ???????????????????????????????????????Client?????????Java??????????????????????????????
    //    ??????Python??????????????????????????????(Python??????????????????)(????????????????????????????????????????????????????????????????????????)
    //    ?????????
    //    ????????????Nginx???????????????????????????????????????(Python??????????????????????????????????????????)?????????????????????????????????????????????????????????
    //*****************************/?????????/*****************************//


    //*****************************/?????????/*****************************//
    //    TODO???
    //    ?????????????????????????????????????????????????????????????????????
    //    ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    //    ?????????
    //    ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    //    ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    //*****************************/?????????/*****************************//


    //*****************************/?????????/*****************************//
    //    ?????????????????????
    //    ???????????????????????????(????????????????????????????????????)
    //    ??????Pdf???????????????????????????????????????????????????????????????
    //    ?????????
    //    ??????????????????Pdf?????????????????????
    //    ?????????????????????????????????????????????????????????????????????????????????????????????
    //*****************************/?????????/*****************************//

    /**
     * ?????????????????????(????????????????????????????????????????????????)
     *
     * @param waybillDtoList ????????????
     * @return
     */
    @PostMapping("/print")
    public void handle(List<WaybillDto> waybillDtoList) {
        parse(waybillDtoList);
    }


    private static String LOGO_IMG = "/static/logo.png";
    private static String GLASS_IMG = "/static/glass.png";
    private static String A = "/static/A.jpg";
    private static String B = "/static/B.jpg";
    private static String E = "/static/E.png";
    private static String COD_IMG = "/static/COD.jpg";
    private static String POD_IMG = "/static/POD.png";

    private static final String HEI_FONT = "/font/SIMHEI.TTF";
    private static final String SUN_FONT = "/font/SIMSUN.TTC,0";

    private static PdfFont heiTi = null;
    private static PdfFont songTi = null;

    static {
        try {
            heiTi = PdfFontFactory.createFont(HEI_FONT, PdfEncodings.IDENTITY_H);
            songTi = PdfFontFactory.createFont(SUN_FONT, PdfEncodings.IDENTITY_H);
        } catch (IOException e) {
            log.error("????????????????????????", e);
        }
    }

    @SneakyThrows
    public static Document create(ByteArrayOutputStream os) {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(os));
        Document document = new Document(pdfDoc, new PageSize(215, 368));
        document.setMargins(0, 0, 0, 0);
        return document;
    }

    public static void frameLayout(Document document) {
        DashedBorder dashedBorder = new DashedBorder(1f);

        Table table = new Table(new float[]{170, 45});

        table.addCell(new Cell(1, 2).setHeight(38)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        table.addCell(new Cell(1, 2).setHeight(67)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        table.addCell(new Cell(1, 2).setHeight(28)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        Cell left = new Cell().setHeight(232)
                .setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0);
        leftLayout(dashedBorder, left);
        table.addCell(left);

        Cell right = new Cell().setHeight(232)
                .setBorder(Border.NO_BORDER).setBorderLeft(dashedBorder)
                .setPaddings(0, 0, 0, 0);
        rightLayout(dashedBorder, right);
        table.addCell(right);

        document.add(table);
    }

    private static void rightLayout(DashedBorder dashedBorder, Cell right) {
        Table tableRight = new Table(new float[]{45});

        tableRight.addCell(new Cell().setHeight(197)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableRight.addCell(new Cell().setHeight(34).setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0));

        right.add(tableRight);
    }

    private static void leftLayout(DashedBorder dashedBorder, Cell left) {
        Table tableLeft = new Table(new float[]{90, 79});

        tableLeft.addCell(new Cell(1, 2).setHeight(52)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableLeft.addCell(new Cell(1, 2).setHeight(21)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        Cell threeLeft = new Cell().setHeight(84)
                .setBorder(Border.NO_BORDER).setBorderRight(dashedBorder).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0);
        middleTable(dashedBorder, threeLeft);
        tableLeft.addCell(threeLeft);

        tableLeft.addCell(new Cell().setHeight(84)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableLeft.addCell(new Cell(1, 2).setHeight(16)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableLeft.addCell(new Cell(1, 2).setHeight(53).setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0));

        left.add(tableLeft);

    }

    private static void middleTable(DashedBorder dashedBorder, Cell threeLeft) {
        Table tableMiddle = new Table(new float[]{90});

        tableMiddle.addCell(new Cell().setHeight(16)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableMiddle.addCell(new Cell().setHeight(32)
                .setBorder(Border.NO_BORDER).setBorderBottom(dashedBorder)
                .setPaddings(0, 0, 0, 0));

        tableMiddle.addCell(new Cell().setHeight(32).setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0));

        threeLeft.add(tableMiddle);
    }

    @SneakyThrows
    public static void fillFixedContent(Document document) {
        document.add(new Image(ImageDataFactory.create(LOGO_IMG), 8f, 340, 56)
                .setWidth(56).setHeight(19));
        document.add(new Paragraph("???").setFixedPosition(5, 179, 15)
                .setFont(songTi).setFontSize(12).setHeight(54));
        document.add(new Paragraph("???").setFixedPosition(5, 156, 15)
                .setFont(songTi).setFontSize(12).setHeight(25));
        document.add(new Paragraph("?????????")
                .setFont(songTi).setFontSize(9).setBold()
                .setFixedPosition(61f, 142, 30));
        document.add(new Paragraph("?????????")
                .setFont(heiTi).setFontSize(9).setBold()
                .setFixedPosition(60, 58, 70));
    }


    /**
     * ????????????
     *
     * @param waybillDtoList ?????????????????????
     */
    public void parse(List<WaybillDto> waybillDtoList) {
        String localDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if (CollectionUtils.isEmpty(waybillDtoList)) return;
        for (int i = 0; i < waybillDtoList.size(); i++) {
            WaybillDto waybillDto = waybillDtoList.get(i);
            String mailNo = waybillDto.getMailNo();

            Optional.ofNullable(mailNo).orElseThrow(() -> new NullPointerException("?????????????????????"));

            String[] mailNos = mailNo.split(",");
            String motherMailNo = mailNos[0];
            for (int j = 0; j < mailNos.length; j++) {
                String s = (j + 1) + "/" + mailNos.length;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                Document document = PrintApplication.create(outputStream);

                PrintApplication.frameLayout(document);

                PrintApplication.fillFixedContent(document);

                PrintApplication.fillVariableContent(document, waybillDto, localDateTime, s,
                        mailNos[j], motherMailNo, document.getPdfDocument(), Objects.equals(1, mailNos.length));

                PrintApplication.print(outputStream.toByteArray());
            }
        }
    }

    /**
     * ?????????????????? TODO:: ??????????????????
     *
     * @param document
     * @param waybillDto
     * @param localDateTime
     * @param no
     * @param expressNo
     * @param motherMailNo
     * @param pdfDoc
     * @param flag
     */
    @SneakyThrows
    public static void fillVariableContent(Document document, WaybillDto waybillDto,
                                           String localDateTime, String no, String expressNo,
                                           String motherMailNo, PdfDocument pdfDoc, boolean flag) {
//        ProCode
        Paragraph proCode = new Paragraph(waybillDto.getProCode().substring(0, 6));
        proCode.setFixedPosition(0, 330, 207)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFont(heiTi).setFontSize(26).setBold();
        document.add(proCode);

//        ?????????????????????
        Paragraph printTime = new Paragraph("ZJ  " + localDateTime + "  ???" + no + "???");
        printTime.setFixedPosition(0, 330, 215)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(songTi).setFontSize(6);
        document.add(printTime);

//        ?????????
        Barcode128 barcode = new Barcode128(pdfDoc);
        barcode.setCode(expressNo);
        barcode.setFont(null);
        barcode.setCodeType(Barcode128.CODE128);
        document.add(new Image(barcode.createFormXObject(pdfDoc))
                .setWidth(187).setHeight(36)
                .setFixedPosition(14, flag ? 290 : 285));

//        ???????????????
        barcode.fitWidth(170);
        barcode.setBarHeight(36);
        document.add(new Image(barcode.createFormXObject(pdfDoc))
                .setRotationAngle(Math.PI / 2).setFixedPosition(171f, 48f));

//        ??????????????????
        Table barTable = new Table(UnitValue.createPercentArray(new float[]{20, 20, 60}));
        barTable.setFixedPosition(14, flag ? 265 : 270, 187)
                .setFont(heiTi).setFontSize(10).setBold();

        if (flag) {
            barTable.addCell(new Cell().setBorder(Border.NO_BORDER));

//            ????????????
            Cell tipCell = new Cell().setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddings(0, 0, 0, 0);
            Paragraph tipParagraph = new Paragraph("?????????").setFixedLeading(12);
            tipCell.add(tipParagraph);
            barTable.addCell(tipCell);

//            ??????
            Cell codeCell = new Cell().setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0);
            Paragraph codeParagraph = new Paragraph(
                    expressNo.replaceAll("(\\w{2})(\\d{3})(\\d{3})(\\d{3})(\\d*)", "$1 $2 $3 $4 $5"))
                    .setFixedLeading(12);
            codeCell.add(codeParagraph);
            barTable.addCell(codeCell);
        }

//        ???????????????
        Cell noCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddings(0, 0, 0, 0);
        Paragraph noParagraph = new Paragraph(no).setFixedLeading(12);
        noCell.add(noParagraph);
        barTable.addCell(noCell);

//        ????????????
        Cell tipCell = new Cell().setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0)
                .setTextAlignment(TextAlignment.CENTER);
        Paragraph tipParagraph = new Paragraph("?????????").setFixedLeading(12);
        tipCell.add(tipParagraph);
        barTable.addCell(tipCell);

//        ??????
        Cell codeCell = new Cell().setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0);
        Paragraph codeParagraph = new Paragraph(
                motherMailNo.replaceAll("(\\w{2})(\\d{3})(\\d{3})(\\d{3})(\\d*)", "$1 $2 $3 $4 $5"))
                .setFixedLeading(12);
        codeCell.add(codeParagraph);

        barTable.addCell(codeCell);
        document.add(barTable);

//        ???????????????
        String code = waybillDto.getQRCode().replace(motherMailNo, expressNo);
        BarcodeQRCode barcodeQRCode = new BarcodeQRCode(code);
        PdfFormXObject formXObject = barcodeQRCode.createFormXObject(new DeviceRgb(Color.black), pdfDoc);
        document.add(new Image(formXObject)
                .setHeight(70).setWidth(70)
                .setFixedPosition(95f, 80, 70));


//        destRoutLabel
        String destRoutLabel = waybillDto.getDestRouteLabel();
        String substring = destRoutLabel.substring(0,
                destRoutLabel.length() > 17 ? 17 : destRoutLabel.length());
        boolean gt12 = substring.length() >= 12;
        Paragraph graph = new Paragraph(substring)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(heiTi).setFontSize(gt12 ? 22 : 30).setBold()
                .setFixedPosition(8, gt12 ? 230 : 225, 200);
        document.add(graph);

//        ?????????
        Table receiverInfoTable = new Table(UnitValue.createPointArray(new float[]{60, 10, 80}))
                .setFontSize(9);
        receiverInfoTable.setFont(songTi).setMaxHeight(54)
                .setFixedPosition(20, 179, 150);

//        ??????
        Cell receiverNameCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setMaxHeight(12).setMaxWidth(60)
                .setPaddings(0, 0, 0, 0);
        Paragraph receiverName = new Paragraph(waybillDto.getConsignerName()).setFixedLeading(11);
        receiverNameCell.add(receiverName);
        receiverInfoTable.addCell(receiverNameCell);

        receiverInfoTable.addCell(new Cell().setBorder(Border.NO_BORDER));

//        ?????????
        Cell receiverPhoneCell = new Cell().setBorder(Border.NO_BORDER)
                .setMaxHeight(12).setMaxWidth(80)
                .setPaddings(0, 0, 0, 0);
        Paragraph receiverPhone = new Paragraph(
                waybillDto.getConsignerTel().replaceAll("(\\d{3})(\\d{4})(\\d*)", "$1****$3"))
                .setFixedLeading(11);
        receiverPhoneCell.add(receiverPhone);
        receiverInfoTable.addCell(receiverPhoneCell);

//        ??????
        Cell receiverAddressCell = new Cell(1, 3)
                .setMaxWidth(150).setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0);
        Paragraph receiverAddress = new Paragraph(waybillDto.getConsignerAddress()).setFixedLeading(9f);
        receiverAddressCell.add(receiverAddress);
        receiverInfoTable.addCell(receiverAddressCell);

        document.add(receiverInfoTable);

//        ?????????

        Table senderInfoTable = new Table(UnitValue.createPointArray(new float[]{60, 10, 80}))
                .setFontSize(6).setFont(songTi).setMaxHeight(25)
                .setFixedPosition(20, 156, 150);

//        ??????
        Cell senderNameCell = new Cell()
                .setMaxHeight(8).setMaxWidth(60)
                .setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0);
        Paragraph senderName = new Paragraph(waybillDto.getDeliverName()).setFixedLeading(8);
        senderNameCell.add(senderName);
        senderInfoTable.addCell(senderNameCell);

        senderInfoTable.addCell(new Cell().setBorder(Border.NO_BORDER));

//        ?????????
        Cell senderPhoneCell = new Cell().setBorder(Border.NO_BORDER)
                .setMaxHeight(8).setMaxWidth(80)
                .setPaddings(0, 0, 0, 0);
        Paragraph senderPhone = new Paragraph(
                waybillDto.getDeliverTel().replaceAll("(\\d{3})(\\d{4})(\\d*)", "$1****$3"))
                .setFixedLeading(8);
        senderPhoneCell.add(senderPhone);
        senderInfoTable.addCell(senderPhoneCell);

//        ??????
        Cell senderAddressCell = new Cell(1, 3)
                .setMaxWidth(150).setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0);
        Paragraph senderAddress = new Paragraph(waybillDto.getDeliverAddress()).setFixedLeading(6);
        senderAddressCell.add(senderAddress);
        senderInfoTable.addCell(senderAddressCell);

        document.add(senderInfoTable);


        Image codImage = new Image(ImageDataFactory.create(COD_IMG), 6f, 132, 28);
//        document.add(codImage);

        Image podImage = new Image(ImageDataFactory.create(POD_IMG), 35f, 132, 25);
//        document.add(podImage);

        switch (waybillDto.getAbFlag()) {
            case "A":
                document.add(new Image(ImageDataFactory.create(A), 10, 110, 28));
                break;
            case "B":
                document.add(new Image(ImageDataFactory.create(B), 10, 110, 28));
                break;
            case "E":
                document.add(new Image(ImageDataFactory.create(E), 10, 110, 28));
                break;
            default:
                break;
        }

        String printIcon = waybillDto.getPrintIcon();

//        ????????????
        document.add(new Image(ImageDataFactory.create(GLASS_IMG), 10, 110, 28));

//        codeMapping
        Paragraph codeMapping = new Paragraph(waybillDto.getCodingMapping())
                .setMaxWidth(90).setMaxHeight(60)
                .setFont(heiTi).setFontSize(40).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(5f, 65, 85)
                .setPaddings(0, 0, 0, 0);
        document.add(codeMapping);

        String pay = "????????????";
        if (Objects.equals(2, waybillDto.getPayMethod())) {
            pay = "??????";
        }
        document.add(new Paragraph(pay).setFixedPosition(8, 53, 60).setFont(songTi).setFontSize(9));

//        proName
        Table proNameTable = new Table(new float[]{25})
                .setFont(songTi).setFontSize(6)
                .setHeight(20).setWidth(25)
                .setFixedPosition(180, 7, 25);

        proNameTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setMaxWidth(25).setMaxHeight(10)
                .setPaddings(0, 0, 0, 0)
                .add(new Paragraph("??????").setFixedLeading(8)));

        proNameTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .setMaxWidth(25).setMaxHeight(10)
                .setPaddings(0, 0, 0, 0)
                .add(new Paragraph("KC24KC24KC24").setFixedLeading(8)));
        document.add(proNameTable);

    }
}
