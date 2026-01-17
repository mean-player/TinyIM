package com.example.demo.Controller;


import com.example.demo.AOP.LimitUpload;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.GetFileResponse;
import com.example.demo.DTO.UploadInitResponse;
import com.example.demo.Service.UploadService;
import com.example.demo.Tools.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
public class UploadController {
    private final UploadService uploadService;
    private Long getUserId(){
        return Long.valueOf(AuthUtil.getUserId());
    }

    @Operation(summary = "",description = "")
    @RequestMapping("/easyUpload")
    @LimitUpload
    public ApiResponse<String> easyUpload(@RequestParam("filehash") String filehash,
                                          @RequestParam("filename") String filename,
                                          @RequestParam("size") String size,
                                          @RequestParam("type") String type,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam("isPrivate") boolean isPrivate){
        Long userId = getUserId();
        if(uploadService.easyUpload(userId,filehash,filename,Long.valueOf(size),type,file,isPrivate)){
            return ApiResponse.success();
        }
        return ApiResponse.fail("上传失败");
    }


    @Operation(summary = "",description = "")
    @RequestMapping("/initUpload")
    @LimitUpload
    public ApiResponse<UploadInitResponse> initUpload(@RequestParam("filehash")String filehash,
                                                      @RequestParam("filename") String filename,
                                                      @RequestParam("size") String size,
                                                      @RequestParam("type") String type,
                                                      @RequestParam("isPrivate") boolean isPrivate,
                                                      @RequestParam("totalParts")Integer totalParts
                                                      ){
        Long userId = getUserId();
        log.info("接收到来自的account为 {}的用户的上传请求，filename： {}",userId,filename);
        UploadInitResponse response = uploadService.initUpload(userId,filehash,filename,Long.valueOf(size),type,isPrivate,totalParts);
        log.info("uploadId: {}",response.getUploadId());
        return ApiResponse.success(response);
    }



    @Operation(summary = "",description = "")
    @RequestMapping("/uploadPart")
    public ApiResponse<Boolean> uploadPart(@RequestParam("filehash") String filehash,
                                           @RequestParam("partNumber") Integer partNumber,
                                           @RequestParam("chunkFile") MultipartFile chunkFile){
        Long userId = getUserId();
        if(uploadService.uploadPart(userId,filehash,partNumber,chunkFile)){
            log.info("{}上传成功",partNumber);
            return ApiResponse.success(true);
        }else{
            return ApiResponse.fail("failed to upload this part"+partNumber);
        }

    }


    @Operation(summary = "",description = "")
    @GetMapping("/getMissingParts")
    public ApiResponse<List<Integer>> getMissingParts(@RequestParam("filehash")String filehash){
        List<Integer>missingParts = uploadService.getMissingParts(getUserId(),filehash);
        return ApiResponse.success(missingParts);
    }


    @Operation(summary = "",description = "")
    @RequestMapping("/completeUpload")
    public ApiResponse<Boolean> completeUpload(@RequestParam("filehash")String filehash){
        Long userId =getUserId();
        if(uploadService.completeUpload(userId,filehash)){
            log.info("user: {} filehash: {} 的文件合并成功",userId,filehash);
            return ApiResponse.success(true);
        }else{
            return ApiResponse.fail("failed to complete this upload,user:"+userId+"filehash: "+filehash);
        }

    }

    @Operation(summary = "",description = "")
    @RequestMapping("/getFileUrl")
    public ApiResponse<GetFileResponse> getFileUrl(@RequestParam("filehash")String filehash){
        GetFileResponse getFileResponse = uploadService.getUrl(getUserId(),filehash);
        if(!(getFileResponse == null)){
            return ApiResponse.success(getFileResponse);
        }
        return ApiResponse.fail("无法得到该文件url");
    }









}
