package cn.xunhou.xbbcloud.rpc.other.service;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.Conditional;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.other.dao.DictionaryRepository;
import cn.xunhou.xbbcloud.rpc.other.entity.DictionaryEntity;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author litb
 * @date 2022/9/19 14:21
 * <p>
 * 字典Service
 */
@Service
@Slf4j
public class DictionaryService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private IRedisLockService redisLockService;

    public ScheduleServerProto.DictionarySaveResponse saveDictionary(ScheduleServerProto.DictionarySaveRequest request) {

        String lockKey = "DIC_LOCK_" + request.getDictionaryType().getNumber() + "_" + XBB_USER_CONTEXT.get().getTenantId();
        try {
            if (!redisLockService.tryLock(lockKey, TimeUnit.SECONDS, 5, 5)) {
                log.error("字典创建异常,未获取到锁,key = {}", lockKey);
                throw GrpcException.asRuntimeException("请稍后重试");
            }

            List<DictionaryEntity> list = dictionaryRepository.findList(XBB_USER_CONTEXT.get().getTenantId(), request.getDictionaryTypeValue(), null);
            Conditional.run(CollUtil.isNotEmpty(list), () -> list.forEach(dictionaryEntity -> {
                if (dictionaryEntity.getName().equals(request.getDictionaryName()) && !dictionaryEntity.getId().equals(request.getId())) {
                    throw GrpcException.runtimeException(Status.ALREADY_EXISTS, "名称已存在");
                }
            }));


            //编辑
            if (request.hasId()) {

                DictionaryEntity existOne = dictionaryRepository.findOneById(request.getId());
                IAssert.notNull(existOne, "未找到对应数据");
                IAssert.state(existOne.getTenantId() != 0, "系统默认数据不可编辑");

                DictionaryEntity updatedEntity = new DictionaryEntity();
                updatedEntity.setModifyBy(XBB_USER_CONTEXT.get().getUserId());
                updatedEntity.setId(request.getId());
                if (request.hasDictionaryDesc()) {
                    updatedEntity.setDescription(request.getDictionaryDesc());
                }
                updatedEntity.setName(request.getDictionaryName());
                dictionaryRepository.updateById(request.getId(), updatedEntity);
                return ScheduleServerProto.DictionarySaveResponse.newBuilder().setId(request.getId()).build();
            }

            DictionaryEntity saveEntity = new DictionaryEntity();
            saveEntity.setType(request.getDictionaryTypeValue());
            saveEntity.setName(request.getDictionaryName());
            saveEntity.setDescription(request.getDictionaryDesc());
            saveEntity.setCreateBy(XBB_USER_CONTEXT.get().getUserId());
            saveEntity.setModifyBy(XBB_USER_CONTEXT.get().getUserId());
            saveEntity.setTenantId(XBB_USER_CONTEXT.get().getTenantId());

            if (CollUtil.isNotEmpty(list)) {
                int code = list.stream().mapToInt(DictionaryEntity::getCode).max().orElse(0) + 1;
                int sort = list.stream().mapToInt(DictionaryEntity::getSort).max().orElse(0) + 1;
                saveEntity.setCode(code);
                saveEntity.setSort(sort);
            } else {
                saveEntity.setCode(1);
                saveEntity.setSort(1);
            }
            Number id = dictionaryRepository.insert(saveEntity);

            return ScheduleServerProto.DictionarySaveResponse.newBuilder().setId((long) id).build();
        } finally {
            //有问题
            redisLockService.unlock(lockKey);
        }
    }

    public ScheduleServerProto.DictionaryListResponse findDictionaryList(ScheduleServerProto.DictionaryListRequest request) {
        Integer tenantId = null;
        if (request.hasTenantId()) {
            tenantId = request.getTenantId();
        } else {
            tenantId = XBB_USER_CONTEXT.tenantId();
        }
        List<DictionaryEntity> list = dictionaryRepository.findList(tenantId, request.getDictionaryTypeValue(), null);

        ScheduleServerProto.DictionaryListResponse.Builder builder = ScheduleServerProto.DictionaryListResponse.newBuilder();

        if (CollUtil.isNotEmpty(list)) {
            List<ScheduleServerProto.DictionaryResponse> responseList = new ArrayList<>();
            list.forEach(dictionaryEntity -> responseList.add(ScheduleServerProto.DictionaryResponse.newBuilder()
                    .setId(dictionaryEntity.getId())
                    .setCode(dictionaryEntity.getCode())
                    .setName(dictionaryEntity.getName())
                    .setDescription(dictionaryEntity.getDescription())
                    .setCreateBy(dictionaryEntity.getCreateBy())
                    .setModifyBy(dictionaryEntity.getModifyBy())
                    .setParentCode(dictionaryEntity.getParentCode())
                    .setSort(dictionaryEntity.getSort())
                    .setEditable(dictionaryEntity.getTenantId() != 0)
                    .setType(dictionaryEntity.getType())
                    .build()));

            builder.addAllDictionaryList(responseList);
        }
        return builder.build();
    }

    public ScheduleServerProto.DictionaryResponse findOne(ScheduleServerProto.DictionaryRequest request) {
        DictionaryEntity dictionaryEntity = dictionaryRepository.findOneById(request.getDictId());
        IAssert.notNull(dictionaryEntity, "字典不存在");
        return ScheduleServerProto.DictionaryResponse.newBuilder()
                .setId(dictionaryEntity.getId())
                .setCode(dictionaryEntity.getCode())
                .setName(dictionaryEntity.getName())
                .setDescription(dictionaryEntity.getDescription())
                .setCreateBy(dictionaryEntity.getCreateBy())
                .setModifyBy(dictionaryEntity.getModifyBy())
                .setParentCode(dictionaryEntity.getParentCode())
                .setSort(dictionaryEntity.getSort())
                .setEditable(dictionaryEntity.getTenantId() != 0)
                .setType(dictionaryEntity.getType())
                .build();
    }
}
