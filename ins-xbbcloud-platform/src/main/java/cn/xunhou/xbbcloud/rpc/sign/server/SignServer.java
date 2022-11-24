package cn.xunhou.xbbcloud.rpc.sign.server;

import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.grpc.proto.xbbcloud.AbstractSignServerImplBase;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.xbbcloud.rpc.sign.pojo.SignConvert;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.ContractResult;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.PositionQrcodeResult;
import cn.xunhou.xbbcloud.rpc.sign.service.SignService;
import com.google.protobuf.Empty;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.annotation.Resource;

/**
 * 签约云
 */
@Slf4j
@GrpcService
public class SignServer extends AbstractSignServerImplBase {

    @Resource
    private SignService signService;

    @Override
    protected SignServerProto.SignInfoListResponse signInfoList(SignServerProto.SignInfoListRequest request) {
        return null;
    }

    @Override
    protected SignServerProto.SignInfoResponse saveSignInfo(SignServerProto.SignInfoRequest request) {
        return signService.saveSignInfo(request);
    }

    @Override
    protected SignServerProto.SignInfoResponse signInfo(SignServerProto.SignInfoIdRequest request) {
        return signService.signInfo(request);
    }

    @Override
    protected SignServerProto.PositionQrcodeSaveResponse saveOrUpdateQrcode(SignServerProto.PositionQrcodeSaveRequest request) {
        return signService.saveOrUpdateQrcode(request);
    }

    @Override
    protected SignServerProto.BatchInsertContractResponse batchInsertContract(SignServerProto.BatchInsertContractRequest request) {
        return signService.batchInsertContract(request);
    }

    @Override
    protected Empty batchUpdateContract(SignServerProto.BatchUpdateContractRequest request) {
        return signService.batchUpdateContract(request);
    }

    @Override
    protected SignServerProto.PagePositionQrcodeResponse positionQrcodeList(SignServerProto.QrcodeListQueryRequest request) {
        log.info("positionQrcodeList入参:" + XbbProtoJsonUtil.toJsonString(request));
        SignServerProto.PagePositionQrcodeResponse.Builder builder = SignServerProto.PagePositionQrcodeResponse.newBuilder();
        PagePojoList<PositionQrcodeResult> pagePojoList = signService.positionQrcodeList(request);
        for (PositionQrcodeResult result : pagePojoList.getData()) {
            builder.addData(SignConvert.result2Response(result));
        }
        builder.setTotal(pagePojoList.getTotal());
        return builder.build();
    }

    @Override
    protected SignServerProto.PositionQrcodeResponse positionQrcodeDetail(SignServerProto.QrcodeDetailQueryRequest request) {
        return signService.positionQrcodeDetail(request);
    }



    @Override
    protected SignServerProto.BestSignUrlResponse getBestSignUrl(SignServerProto.GetBestSignUrlRequest request) {
        return signService.getBestSignUrl(request);
    }

    @Override
    protected SignServerProto.ContractPageListResponse contractList(SignServerProto.ContractListRequest request) {
        log.info("contractList入参:" + XbbProtoJsonUtil.toJsonString(request));
        SignServerProto.ContractPageListResponse.Builder builder = SignServerProto.ContractPageListResponse.newBuilder();
        PagePojoList<ContractResult> pagePojoList = signService.contractList(request);
        for (ContractResult result : pagePojoList.getData()) {
            builder.addData(SignConvert.result2Response(result));
        }
        builder.setTotal(pagePojoList.getTotal());
        return builder.build();
    }


    @SneakyThrows
    @Override
    protected Empty contractSignComplete(SignServerProto.ContractSignCompleteRequest request) {
        signService.contractSignComplete(request);
        return Empty.newBuilder().build();
    }

    @SneakyThrows
    @Override
    protected Empty initTemplate(SignServerProto.InitTemplateListRequest request) {
        signService.initTemplate(request);
        return Empty.newBuilder().build();
    }

    @SneakyThrows
    @Override
    protected SignServerProto.saveContractPdfToOssResponse saveContractPdfToOss(SignServerProto.saveContractPdfToOssRequest saveContractPdfToOssRequest) {
        String ossId = signService.saveContractPdfToOss(saveContractPdfToOssRequest.getServiceBusinessId());
        return SignServerProto.saveContractPdfToOssResponse.newBuilder().setOssId(ossId).build();
    }

}
