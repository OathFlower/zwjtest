package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.sxz.dto.PackageResult;
import cn.xunhou.web.xbbcloud.product.sxz.enums.RmbPackageEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.ServicePackageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 省薪招-产品相关
 */
@RequestMapping("/api/products")
@Slf4j
@RestController
public class ProductController {
    /**
     * 服务包列表
     *
     * @return
     */
    @GetMapping("/package/service")
    public JsonResponse<List<PackageResult>> servicePackage() {
        ServicePackageEnum[] values = ServicePackageEnum.values();
        List<PackageResult> resultList = new ArrayList<>();
        for (ServicePackageEnum servicePackageEnum : values) {
            PackageResult packageResult = new PackageResult();
            packageResult.setCode(servicePackageEnum.getCode()).setMsg(servicePackageEnum.getMsg())
                    .setOriginCoin(servicePackageEnum.getOriginCoin()).setNowCoin(servicePackageEnum.getNowCoin());
            resultList.add(packageResult);
        }
        return JsonResponse.success(resultList);
    }


    /**
     * 资源包(价格)列表
     *
     * @return
     */
    @GetMapping("/package/price")
    public JsonResponse<List<PackageResult>> rmbPackage() {
        RmbPackageEnum[] values = RmbPackageEnum.values();
        List<PackageResult> resultList = new ArrayList<>();
        for (RmbPackageEnum rmbPackageEnum : values) {
            PackageResult packageResult = new PackageResult();
            packageResult.setCode(rmbPackageEnum.getCode()).setMsg(rmbPackageEnum.getMsg())
                    .setOriginCoin(rmbPackageEnum.getOriginCoin()).setNowCoin(rmbPackageEnum.getNowCoin());
            resultList.add(packageResult);
        }
        return JsonResponse.success(resultList);
    }
}
