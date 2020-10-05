package com.github.gdl;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.validation.constraints.NotNull;
import java.awt.print.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = "*")
public class DirectPrintController {
    private static final Logger log = LoggerFactory.getLogger(DirectPrintController.class);

    @GetMapping("/printers")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Available printers")
    ResponseEntity printers() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        log.info("Available printers: " + Arrays.asList(services));
        return new ResponseEntity<>(Arrays.stream(services).map(PrintService::getName), HttpStatus.OK);
    }

    @GetMapping("/printer/default")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get Default printer")
    ResponseEntity defaults() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        return new ResponseEntity<>(service.getName(), HttpStatus.OK);
    }

    @PostMapping("/printer/default")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Set Default printer")
    ResponseEntity defaults(@RequestParam String printerName) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        Arrays.stream(printServices).filter(printer -> printer.getName().equals(printerName)).forEach(printer -> {
            try {
                job.setPrintService(printer);
            } catch (PrinterException e) {}
        });
        return new ResponseEntity<>("Succeeded", HttpStatus.OK);
    }

    @PostMapping(value = "/print")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Printing raw data")
    ResponseEntity print(
            @Parameter(description = "Base64 encoded") @RequestParam @NotNull String data,
            @RequestParam @NotNull Scaling scaling,
            @RequestParam @NotNull boolean showPageBorder,
            @RequestParam(value = "dpi", required=false, defaultValue = "300") float dpi,
            @RequestParam(value = "width", required=false, defaultValue = "595") double width,
            @RequestParam(value = "height", required=false, defaultValue = "842") double height,
            @RequestParam @NotNull Orientation orientation,
            @RequestParam @NotNull boolean withPrintDialog) throws IOException, PrinterException {
        byte[] bytes = Base64.getDecoder().decode(data);
        this.printJob(bytes);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping(value = "/print/binary" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Printing binary data")
    ResponseEntity printBinary(
            @RequestPart("file") MultipartFile file,
            @RequestParam @NotNull Scaling scaling,
            @RequestParam @NotNull boolean showPageBorder,
            @RequestParam(value = "dpi", required=false, defaultValue = "300") float dpi,
            @RequestParam(value = "width", required=false, defaultValue = "595") double width,
            @RequestParam(value = "height", required=false, defaultValue = "842") double height,
            @RequestParam @NotNull Orientation orientation,
            @RequestParam @NotNull boolean withPrintDialog) throws IOException, PrinterException {
        this.printJob(file.getBytes());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping(value = "/print/url")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Printing form URL")
    ResponseEntity printUri(
            @RequestParam @NotNull String url,
            @RequestParam @NotNull Scaling scaling,
            @RequestParam @NotNull boolean showPageBorder,
            @RequestParam(value = "dpi", required=false, defaultValue = "300") float dpi,
            @RequestParam(value = "width", required=false, defaultValue = "595") double width,
            @RequestParam(value = "height", required=false, defaultValue = "842") double height,
            @RequestParam @NotNull Orientation orientation,
            @RequestParam @NotNull boolean withPrintDialog) throws IOException, PrinterException {
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        log.info(urlConnection.getContentType());
        byte[] bytes = StreamUtils.copyToByteArray(urlConnection.getInputStream());
        this.printJob(bytes, scaling, showPageBorder, dpi, width, height, orientation, withPrintDialog);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    void printJob(byte[] bytes) throws IOException, PrinterException {
        this.printJob(bytes, Scaling.SHRINK_TO_FIT, false, 300, 595, 842, Orientation.PORTRAIT,false);
    }

    void printJob(byte[] bytes, Scaling scaling, boolean showPageBorder, float dpi, double width, double height, Orientation orientation, boolean withPrintDialog) throws IOException, PrinterException {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) {
            throw new IllegalStateException("No default print service found.");
        }

        PDDocument doc = PDDocument.load(bytes);
        PDFPrintable printable = new PDFPrintable(doc, scaling, showPageBorder, dpi);

        PrinterJob job = PrinterJob.getPrinterJob();
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());

        PageFormat pageFormat = new PageFormat(); // or printerJob.defaultPage();
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(orientation.getValue());

        Book book = new Book();
        book.append(printable, pageFormat, doc.getNumberOfPages());
        job.setPageable(book);

        if (withPrintDialog) {
            if (job.printDialog()) {
                job.print();
            }
        }
        else {
            job.print();
        }

        doc.close();
    }

}
