package flexistock.inventory.controller;

import flexistock.inventory.dto.ReceiptResponseDto;
import flexistock.inventory.entity.nosql.ReceiptDocument;
import flexistock.inventory.service.AuthenticatedUser;
import flexistock.inventory.service.UserValidationService;
import flexistock.inventory.service.nosql.NoSqlReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/nosql")
public class NoSqlReceiptController {
    private static final Logger logger = LoggerFactory.getLogger(NoSqlReceiptController.class);
    private static final DateTimeFormatter US_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final NoSqlReceiptService receiptService;
    private final UserValidationService userValidationService;

    public NoSqlReceiptController(NoSqlReceiptService receiptService, UserValidationService userValidationService) {
        this.receiptService = receiptService;
        this.userValidationService = userValidationService;
    }

    @PostMapping(value = "/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptResponseDto uploadReceipt(
            @RequestHeader("X-Auth-Token") String token,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "receiptFile", required = false) MultipartFile receiptFile,
            @RequestPart(value = "pdfReceipt", required = false) MultipartFile pdfReceipt,
            @RequestParam("storeName") String storeName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("receiptDate") String receiptDate
    ) {
        AuthenticatedUser user = userValidationService.validate(token);
        MultipartFile resolvedFile = pickFile(file, receiptFile, pdfReceipt);
        if (resolvedFile == null || resolvedFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF file is required");
        }
        if (storeName == null || storeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storeName is required");
        }
        if (!isPdf(resolvedFile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF receipts are allowed");
        }

        LocalDate parsedDate = parseReceiptDate(receiptDate);

        byte[] fileData;
        try {
            fileData = resolvedFile.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file", ex);
        }

        logger.info("Receipt upload request userId={} storeName={} date={} fileName={}", user.id(), storeName, parsedDate, resolvedFile.getOriginalFilename());
        String normalizedStoreName = storeName.trim();
        String generatedFileName = buildReceiptFileName(parsedDate, normalizedStoreName);
        return receiptService.createReceipt(
                user,
                generatedFileName,
                resolvedFile.getContentType(),
                resolvedFile.getSize(),
                fileData,
                normalizedStoreName,
                description,
                parsedDate
        );
    }

    @GetMapping("/receipts")
    public List<ReceiptResponseDto> getReceipts(@RequestHeader("X-Auth-Token") String token) {
        userValidationService.validate(token);
        return receiptService.getReceipts();
    }

    @GetMapping("/receipts/{id}/file")
    public ResponseEntity<ByteArrayResource> downloadReceiptFile(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id
    ) {
        userValidationService.validate(token);
        ReceiptDocument receipt = receiptService.getReceipt(id);
        byte[] fileData = receipt.getFileData() == null ? new byte[0] : receipt.getFileData();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(receipt.getFileName() == null ? "receipt.pdf" : receipt.getFileName())
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(fileData.length)
                .body(new ByteArrayResource(fileData));
    }

    private MultipartFile pickFile(MultipartFile file, MultipartFile receiptFile, MultipartFile pdfReceipt) {
        if (file != null) {
            return file;
        }
        if (receiptFile != null) {
            return receiptFile;
        }
        return pdfReceipt;
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            return true;
        }
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    private LocalDate parseReceiptDate(String receiptDate) {
        if (receiptDate == null || receiptDate.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receiptDate is required");
        }

        try {
            return LocalDate.parse(receiptDate);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(receiptDate, US_DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receiptDate must be in YYYY-MM-DD or MM/DD/YYYY format", ex);
            }
        }
    }

    private String buildReceiptFileName(LocalDate receiptDate, String storeName) {
        String safeStoreName = storeName
                .trim()
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .toLowerCase(Locale.ROOT);

        if (safeStoreName.isBlank()) {
            safeStoreName = "store";
        }

        return receiptDate.format(FILE_DATE_FORMATTER) + "-" + safeStoreName + ".pdf";
    }
}
