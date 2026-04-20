package com.example.BackEndServer.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.BackEndServer.server.dto.UploadCsvInputDto;

@RestController
@RequestMapping("/spring-boot-api")
public class UploadCsvController {

    @Autowired
    UploadCsvService uploadCsvService;


    @PostMapping("/upload-csv")
    public String uploadCsv(@RequestParam("name") String name, @RequestParam("file") MultipartFile file) {
        UploadCsvInputDto input = UploadCsvInputDto.builder()
                .name(name)
                .file(file)
                .build();
        uploadCsvService.uploadCsv(input);
        return "redirect:/";
    }

}
