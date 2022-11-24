# 薪班班Saas

遵循 [薪班班JAVA开发规范](https://g22h5luj8j.feishu.cn/docx/doxcn6mkYnPgP4lREtFXgK8p98r)

### 目录结构:
proto:
存放proto文件,生成grpc文件

### 开发约定:

### 异常:
抛出业务异常请使用cn.xunhou.xbbcloud.common.exception.XbbCloudException,异常状态码请使用cn.xunhou.xbbcloud.common.exception.XbbCloudErrorCode

### GRPC:
grpc接口返回字段不能为null,请使用cn.xunhou.xbbcloud.common.utils.ConvertUtil工具包装后返回

### 事务:
请在Service层和Dao层使用事务,禁止在Server层使用事务(不支持)

### 常用工具命令
#### 安装zctl
`zctl install`
#### 生成client包
`zctl java_grpc_package --p proto --v 0.0.5-SNAPSHOT`
#### 生成文档
html :`zctl doc --s proto --t 0`
md :`zctl doc --s proto --t 1`
#### SQL生成Dao&Entity
`zctl sql_to_dao --p proto`
#### SQL生成proto包装类
`zctl sql_to_proto --p proto`


## feature-xcy-2.0
[薪酬云—无卡发薪2.0](https://g22h5luj8j.feishu.cn/docx/Ng8cdzfEjorZLTxsW3ochqd5n3e)

### 01-运营后台-租户管理
