package com.yeoun.pay.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.yeoun.pay.service.PayPdfService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay/pdf")
public class PayPdfController {

    private final PayPdfService payPdfService;

    /** 🔥 새 탭에서 열기(미리보기) + 다운로드 가능 */
    @GetMapping("/{payslipId}")
    public ResponseEntity<byte[]> viewPdf(@PathVariable("payslipId") Long payslipId) {

        byte[] pdf = payPdfService.generatePdf(payslipId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        // 🔥 브라우저에서 바로 열기 (inline)
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("payslip_" + payslipId + ".pdf")
                        .build()
        );

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
