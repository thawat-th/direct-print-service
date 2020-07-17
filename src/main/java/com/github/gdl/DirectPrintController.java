package com.github.gdl;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/v1/drp")
@Slf4j
@CrossOrigin(origins = "*")
public class DirectPrintController {

    @GetMapping("/printers")
    @ApiOperation(value = "Available printers")
    public ResponseEntity printers() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        log.info("Available printers: " + Arrays.asList(services));
        return new ResponseEntity<>( Arrays.stream(services).map(PrintService::getName), HttpStatus.OK);
    }

    @GetMapping("/printer/default")
    @ApiOperation(value = "Get Default printer")
    public ResponseEntity defaultp() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        return new ResponseEntity<>(service, HttpStatus.OK);
    }

    @PostMapping("/print")
    @ApiOperation(value = "Printing")
    public ResponseEntity print( @RequestParam @NotNull MultipartFile file) throws IOException, PrintException {
        this.printJob(file.getBytes());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping("/print/binary")
    @ApiOperation(value = "Printing a base64 encoded")
    public ResponseEntity printBinary(@NotNull String data) throws PrintException {
        byte[] bytes  = Base64.decodeBase64(data);
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
