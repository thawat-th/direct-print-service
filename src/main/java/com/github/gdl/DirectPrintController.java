package com.github.gdl;


import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
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
    @Operation(summary = "Available printers")
    public ResponseEntity printers() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        log.info("Available printers: " + Arrays.asList(services));
        return new ResponseEntity<>(Arrays.stream(services).map(PrintService::getName), HttpStatus.OK);
    }

    @GetMapping("/printer/default")
    @Operation(summary = "Get Default printer")
    public ResponseEntity defaults() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        return new ResponseEntity<>(service.getName(), HttpStatus.OK);
    }

    @PostMapping(value = "/print", consumes = "multipart/form-data")
    @Operation(summary = "Printing")
    public ResponseEntity print(@RequestParam @NotNull MultipartFile file) throws IOException, PrintException {
        this.printJob(file.getBytes());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping(value = "/print/binary", consumes = "multipart/form-data")
    @Operation(summary = "Printing a base64 encoded")
    public ResponseEntity printBinary(@RequestParam @NotNull String data) throws PrintException {
        byte[] bytes = Base64.getDecoder().decode(data);
        this.printJob(bytes);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping("/print/url")
    @Operation(summary = "Print form URL")
    public ResponseEntity printUri(@RequestParam @NotNull String url) throws PrintException, IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        byte[] bytes = StreamUtils.copyToByteArray(urlConnection.getInputStream());
        this.printJob(bytes);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    private void printJob(byte[] bytes) throws PrintException {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) {
            throw new IllegalStateException("Printer not found");
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        attributeSet.add(new Copies(1));

        DocPrintJob job = service.createPrintJob();
        Doc print = new SimpleDoc( byteArrayInputStream, flavor, null );

        job.print(print, null );
    }


}
