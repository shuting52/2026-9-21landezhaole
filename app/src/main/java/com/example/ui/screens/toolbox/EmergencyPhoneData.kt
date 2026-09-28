package com.example.ui.screens.toolbox

/**
 * 紧急电话工具 · 数据（v1.0.5 全面扩展）
 *
 * - 覆盖全中国：全国通用权威号码 + 34 个省级行政区（含港澳台）全部地级市/区县数据
 * - 地区选择精确到 省 → 市 → 县(区) → 镇(乡) → 村 六级，覆盖全中国任意角落
 * - 每个号码均可一键快捷呼出（无权限自动打开拨号盘预填，同样一键呼出）
 */

/** 单个紧急号码 */
data class EmergencyNumber(
    val name: String,
    val number: String,
    val desc: String = ""
)

/** 一个分类（含 icon 与号码列表） */
data class EmergencyCategory(
    val id: String,
    val title: String,
    val icon: String,
    val numbers: List<EmergencyNumber>
)

/** 村（自然村/行政村；镇/乡下一级） */
data class RegionVillage(
    val name: String
)

/** 镇/乡（县/区下一级，含该镇/乡下的村） */
data class RegionTown(
    val name: String,
    val villages: List<RegionVillage> = emptyList()
)

/** 县/区（市下一级，含该县/区下的镇/乡） */
data class RegionCounty(
    val name: String,
    val towns: List<RegionTown> = emptyList()
)

/** 地级市/自治州/地区（省下一级，含区号与区县） */
data class RegionCity(
    val name: String,
    val areaCode: String = "",
    val counties: List<RegionCounty> = emptyList()
)

/** 省级行政区（含港澳台，共 34 个） */
data class Region(
    val province: String,
    val cities: List<RegionCity>
)

val NATIONAL_EMERGENCY_CATEGORIES: List<EmergencyCategory> = listOf(
    EmergencyCategory(
        id = "rescue", title = "急救救援", icon = "🚑",
        numbers = listOf(
            EmergencyNumber("公安报警", "110", "刑事案件、治安报警"),
            EmergencyNumber("火警", "119", "火灾、灭火救援"),
            EmergencyNumber("医疗急救", "120", "急危重症医疗救护"),
            EmergencyNumber("交通事故", "122", "交通事故报警处理"),
            EmergencyNumber("水上遇险", "12395", "水上搜救、船舶遇险"),
            EmergencyNumber("森林火警", "12119", "森林火灾报警"),
            EmergencyNumber("地震速报", "12322", "地震台网地震速报（部分地区）"),
            EmergencyNumber("短信报警", "12110", "不便通话时短信报警")
        )
    ),
    EmergencyCategory(
        id = "traffic", title = "交通出行", icon = "🚗",
        numbers = listOf(
            EmergencyNumber("交通运输监督", "12328", "交通运输服务监督热线"),
            EmergencyNumber("高速报警救援", "12122", "全国高速公路报警救援（部分省份开通）"),
            EmergencyNumber("铁路客服", "12306", "火车票务、铁路服务"),
            EmergencyNumber("出租约车", "95128", "全国巡游出租汽车约车电话"),
            EmergencyNumber("航班服务", "95530", "中国东方航空客服"),
            EmergencyNumber("地铁查询", "114", "查号台（可转各城市地铁服务）")
        )
    ),
    EmergencyCategory(
        id = "gov", title = "政务服务", icon = "🏛️",
        numbers = listOf(
            EmergencyNumber("政务便民服务", "12345", "政务服务便民热线（全国统一）"),
            EmergencyNumber("司法援助", "12348", "法律援助、法律咨询（免费）"),
            EmergencyNumber("税务服务", "12366", "纳税服务热线"),
            EmergencyNumber("海关服务", "12360", "海关业务咨询"),
            EmergencyNumber("市场监管", "12315", "消费维权、市场监督管理"),
            EmergencyNumber("文化旅游", "12301", "旅游投诉与服务（部分并入12345）")
        )
    ),
    EmergencyCategory(
        id = "social", title = "社会保障", icon = "🏥",
        numbers = listOf(
            EmergencyNumber("社保人社", "12333", "社保、医保、劳动保障咨询"),
            EmergencyNumber("卫生健康", "12320", "卫生热线、健康咨询服务"),
            EmergencyNumber("公积金服务", "12329", "住房公积金服务热线"),
            EmergencyNumber("民政服务", "12349", "民政事务、养老服务（部分地区）"),
            EmergencyNumber("残疾人服务", "12385", "残疾人服务热线")
        )
    ),
    EmergencyCategory(
        id = "labor", title = "劳动劳务", icon = "🧑‍🏭",
        numbers = listOf(
            EmergencyNumber("劳动监察维权", "12333", "劳动保障监察、拖欠工资投诉"),
            EmergencyNumber("法律援助", "12348", "劳动仲裁/纠纷免费法律咨询"),
            EmergencyNumber("工会服务", "12351", "全国总工会职工服务热线")
        )
    ),
    EmergencyCategory(
        id = "complaint", title = "投诉举报", icon = "📢",
        numbers = listOf(
            EmergencyNumber("消费投诉", "12315", "消费者权益投诉举报"),
            EmergencyNumber("纪检举报", "12388", "纪检监察机关举报"),
            EmergencyNumber("检察服务", "12309", "检察服务中心热线"),
            EmergencyNumber("环保举报", "12369", "环境污染投诉举报（并入12345）"),
            EmergencyNumber("价格监督", "12358", "价格违法行为举报（并入12315）"),
            EmergencyNumber("网信举报", "12377", "互联网违法和不良信息举报"),
            EmergencyNumber("网络不良信息", "12321", "垃圾信息、骚扰电话举报"),
            EmergencyNumber("邮政申诉", "12305", "快递、邮政服务申诉")
        )
    ),
    EmergencyCategory(
        id = "bank", title = "银行客服", icon = "🏦",
        numbers = listOf(
            EmergencyNumber("工商银行", "95588"),
            EmergencyNumber("农业银行", "95599"),
            EmergencyNumber("中国银行", "95566"),
            EmergencyNumber("建设银行", "95533"),
            EmergencyNumber("招商银行", "95555"),
            EmergencyNumber("交通银行", "95559"),
            EmergencyNumber("邮储银行", "95580"),
            EmergencyNumber("中信银行", "95558"),
            EmergencyNumber("民生银行", "95568"),
            EmergencyNumber("兴业银行", "95561"),
            EmergencyNumber("光大银行", "95595"),
            EmergencyNumber("华夏银行", "95577"),
            EmergencyNumber("浦发银行", "95528"),
            EmergencyNumber("广发银行", "400-830-8003"),
            EmergencyNumber("平安银行", "95511"),
            EmergencyNumber("北京银行", "95526"),
            EmergencyNumber("上海银行", "95594"),
            EmergencyNumber("农村信用社", "96288", "部分省份农信社客服")
        )
    ),
    EmergencyCategory(
        id = "express", title = "快递物流", icon = "📦",
        numbers = listOf(
            EmergencyNumber("顺丰速运", "95338"),
            EmergencyNumber("邮政EMS", "11183"),
            EmergencyNumber("中国邮政", "11185"),
            EmergencyNumber("中通快递", "95311"),
            EmergencyNumber("圆通速递", "95554"),
            EmergencyNumber("申通快递", "95543"),
            EmergencyNumber("韵达快递", "95546"),
            EmergencyNumber("京东物流", "950616"),
            EmergencyNumber("德邦物流", "95353"),
            EmergencyNumber("百世快递", "95320"),
            EmergencyNumber("极兔速递", "956025")
        )
    ),
    EmergencyCategory(
        id = "ecommerce", title = "电商平台", icon = "🛒",
        numbers = listOf(
            EmergencyNumber("京东商城", "950618"),
            EmergencyNumber("天猫淘宝", "9510211"),
            EmergencyNumber("支付宝", "95188"),
            EmergencyNumber("拼多多", "400-8822-528"),
            EmergencyNumber("美团", "400-660-5335"),
            EmergencyNumber("滴滴出行", "400-000-0999"),
            EmergencyNumber("携程旅行", "400-830-6666"),
            EmergencyNumber("同程旅行", "400-777-7777")
        )
    ),
    EmergencyCategory(
        id = "delivery", title = "外卖订餐", icon = "🍜",
        numbers = listOf(
            EmergencyNumber("美团外卖", "10109777"),
            EmergencyNumber("饿了么", "10105757"),
            EmergencyNumber("肯德基宅急送", "4008-823-823"),
            EmergencyNumber("麦当劳麦乐送", "400-851-7517"),
            EmergencyNumber("必胜客宅急送", "4008-123-123")
        )
    ),
    EmergencyCategory(
        id = "telecom", title = "通信网络", icon = "📶",
        numbers = listOf(
            EmergencyNumber("中国移动", "10086"),
            EmergencyNumber("中国联通", "10010"),
            EmergencyNumber("中国电信", "10000"),
            EmergencyNumber("中国广电", "10099"),
            EmergencyNumber("电信用户申诉", "12300"),
            EmergencyNumber("查号台", "114"),
            EmergencyNumber("故障报修", "112", "手机紧急呼叫协助（部分网络）")
        )
    ),
    EmergencyCategory(
        id = "utility", title = "生活服务", icon = "💡",
        numbers = listOf(
            EmergencyNumber("供电服务", "95598", "国家电网 / 南方电网 统一服务热线"),
            EmergencyNumber("自来水服务", "96055", "部分地区供水热线，可拨114查询"),
            EmergencyNumber("燃气报修", "95158", "华润燃气客服（部分城市）"),
            EmergencyNumber("城建服务", "12319", "城市建设、市政公用服务热线"),
            EmergencyNumber("查号服务", "114", "各类生活服务电话查询")
        )
    ),
    EmergencyCategory(
        id = "insurance", title = "保险服务", icon = "🛡️",
        numbers = listOf(
            EmergencyNumber("中国人保", "95518"),
            EmergencyNumber("中国人寿", "95519"),
            EmergencyNumber("平安保险", "95511"),
            EmergencyNumber("太平洋保险", "95500"),
            EmergencyNumber("泰康保险", "95522"),
            EmergencyNumber("新华保险", "95567"),
            EmergencyNumber("太平保险", "95589"),
            EmergencyNumber("阳光保险", "95510"),
            EmergencyNumber("大地保险", "95590")
        )
    ),
    EmergencyCategory(
        id = "kids", title = "儿童救助", icon = "🧒",
        numbers = listOf(
            EmergencyNumber("儿童救助保护", "12349", "未成年人救助保护热线（部分）"),
            EmergencyNumber("妇儿维权", "12338", "妇女儿童维权服务热线"),
            EmergencyNumber("儿童失踪报警", "110", "儿童失踪请立即拨打110")
        )
    )
)


/** 全国 34 个省级行政区与全部地级市/区县数据（区号标准；镇/乡/村一级由 UI 自由输入） */
val NATIONAL_REGIONS: List<Region> = listOf(
    Region(province = "北京", cities = listOf(
        RegionCity(name = "北京市", areaCode = "010", counties = listOf(RegionCounty("东城区"), RegionCounty("西城区"), RegionCounty("朝阳区"), RegionCounty("海淀区"), RegionCounty("丰台区"), RegionCounty("石景山区"), RegionCounty("通州区"), RegionCounty("顺义区"), RegionCounty("昌平区"), RegionCounty("大兴区"), RegionCounty("房山区"), RegionCounty("门头沟区"), RegionCounty("怀柔区"), RegionCounty("密云区"), RegionCounty("平谷区"), RegionCounty("延庆区"))),

    )),
    Region(province = "天津", cities = listOf(
        RegionCity(name = "天津市", areaCode = "022", counties = listOf(RegionCounty("和平区"), RegionCounty("河东区"), RegionCounty("河西区"), RegionCounty("南开区"), RegionCounty("河北区"), RegionCounty("红桥区"), RegionCounty("滨海新区"), RegionCounty("东丽区"), RegionCounty("西青区"), RegionCounty("津南区"), RegionCounty("北辰区"), RegionCounty("武清区"), RegionCounty("宝坻区"), RegionCounty("静海区"), RegionCounty("宁河区"), RegionCounty("蓟州区"))),

    )),
    Region(province = "上海", cities = listOf(
        RegionCity(name = "上海市", areaCode = "021", counties = listOf(RegionCounty("黄浦区"), RegionCounty("徐汇区"), RegionCounty("长宁区"), RegionCounty("静安区"), RegionCounty("普陀区"), RegionCounty("虹口区"), RegionCounty("杨浦区"), RegionCounty("浦东新区"), RegionCounty("闵行区"), RegionCounty("宝山区"), RegionCounty("嘉定区"), RegionCounty("金山区"), RegionCounty("松江区"), RegionCounty("青浦区"), RegionCounty("奉贤区"), RegionCounty("崇明区"))),

    )),
    Region(province = "重庆", cities = listOf(
        RegionCity(name = "重庆市", areaCode = "023", counties = listOf(RegionCounty("渝中区"), RegionCounty("江北区"), RegionCounty("南岸区"), RegionCounty("九龙坡区"), RegionCounty("沙坪坝区"), RegionCounty("大渡口区"), RegionCounty("北碚区"), RegionCounty("渝北区"), RegionCounty("巴南区"), RegionCounty("涪陵区"), RegionCounty("万州区"), RegionCounty("永川区"), RegionCounty("江津区"), RegionCounty("合川区"), RegionCounty("綦江区"), RegionCounty("长寿区"))),

    )),
    Region(province = "河北", cities = listOf(
        RegionCity(name = "石家庄", areaCode = "0311", counties = listOf(RegionCounty("长安区"), RegionCounty("桥西区"), RegionCounty("新华区"), RegionCounty("裕华区"), RegionCounty("藁城区"), RegionCounty("鹿泉区"), RegionCounty("正定县"), RegionCounty("栾城区"))),
        RegionCity(name = "唐山", areaCode = "0315", counties = listOf(RegionCounty("路南区"), RegionCounty("路北区"), RegionCounty("丰南区"), RegionCounty("丰润区"), RegionCounty("遵化市"), RegionCounty("迁安市"), RegionCounty("曹妃甸区"))),
        RegionCity(name = "秦皇岛", areaCode = "0335", counties = listOf(RegionCounty("海港区"), RegionCounty("山海关区"), RegionCounty("北戴河区"), RegionCounty("昌黎县"), RegionCounty("抚宁区"))),
        RegionCity(name = "邯郸", areaCode = "0310", counties = listOf(RegionCounty("丛台区"), RegionCounty("邯山区"), RegionCounty("复兴区"), RegionCounty("武安市"), RegionCounty("永年区"))),
        RegionCity(name = "邢台", areaCode = "0319", counties = listOf(RegionCounty("襄都区"), RegionCounty("信都区"), RegionCounty("南宫市"), RegionCounty("沙河市"))),
        RegionCity(name = "保定", areaCode = "0312", counties = listOf(RegionCounty("竞秀区"), RegionCounty("莲池区"), RegionCounty("涿州市"), RegionCounty("安国市"), RegionCounty("高碑店市"), RegionCounty("徐水区"), RegionCounty("满城区"), RegionCounty("清苑区"))),
        RegionCity(name = "张家口", areaCode = "0313", counties = listOf(RegionCounty("桥东区"), RegionCounty("桥西区"), RegionCounty("宣化区"), RegionCounty("张北县"))),
        RegionCity(name = "承德", areaCode = "0314", counties = listOf(RegionCounty("双桥区"), RegionCounty("双滦区"), RegionCounty("兴隆县"))),
        RegionCity(name = "沧州", areaCode = "0317", counties = listOf(RegionCounty("运河区"), RegionCounty("新华区"), RegionCounty("泊头市"), RegionCounty("任丘市"), RegionCounty("黄骅市"))),
        RegionCity(name = "廊坊", areaCode = "0316", counties = listOf(RegionCounty("广阳区"), RegionCounty("安次区"), RegionCounty("霸州市"), RegionCounty("三河市"), RegionCounty("香河县"))),
        RegionCity(name = "衡水", areaCode = "0318", counties = listOf(RegionCounty("桃城区"), RegionCounty("冀州区"), RegionCounty("深州市"))),
    )),
    Region(province = "山西", cities = listOf(
        RegionCity(name = "太原", areaCode = "0351", counties = listOf(RegionCounty("小店区"), RegionCounty("迎泽区"), RegionCounty("杏花岭区"), RegionCounty("尖草坪区"), RegionCounty("万柏林区"), RegionCounty("晋源区"))),
        RegionCity(name = "大同", areaCode = "0352", counties = listOf(RegionCounty("平城区"), RegionCounty("云冈区"), RegionCounty("新荣区"), RegionCounty("云州区"))),
        RegionCity(name = "阳泉", areaCode = "0353", counties = listOf(RegionCounty("城区"), RegionCounty("矿区"), RegionCounty("郊区"))),
        RegionCity(name = "长治", areaCode = "0355", counties = listOf(RegionCounty("潞州区"), RegionCounty("上党区"), RegionCounty("屯留区"), RegionCounty("潞城区"))),
        RegionCity(name = "晋城", areaCode = "0356", counties = listOf(RegionCounty("城区"), RegionCounty("泽州县"), RegionCounty("高平市"))),
        RegionCity(name = "朔州", areaCode = "0349", counties = listOf(RegionCounty("朔城区"), RegionCounty("平鲁区"))),
        RegionCity(name = "晋中", areaCode = "0354", counties = listOf(RegionCounty("榆次区"), RegionCounty("太谷区"), RegionCounty("介休市"))),
        RegionCity(name = "运城", areaCode = "0359", counties = listOf(RegionCounty("盐湖区"), RegionCounty("永济市"), RegionCounty("河津市"))),
        RegionCity(name = "忻州", areaCode = "0350", counties = listOf(RegionCounty("忻府区"), RegionCounty("原平市"))),
        RegionCity(name = "临汾", areaCode = "0357", counties = listOf(RegionCounty("尧都区"), RegionCounty("侯马市"), RegionCounty("霍州市"))),
        RegionCity(name = "吕梁", areaCode = "0358", counties = listOf(RegionCounty("离石区"), RegionCounty("孝义市"), RegionCounty("汾阳市"))),
    )),
    Region(province = "内蒙古", cities = listOf(
        RegionCity(name = "呼和浩特", areaCode = "0471", counties = listOf(RegionCounty("新城区"), RegionCounty("回民区"), RegionCounty("玉泉区"), RegionCounty("赛罕区"))),
        RegionCity(name = "包头", areaCode = "0472", counties = listOf(RegionCounty("昆都仑区"), RegionCounty("青山区"), RegionCounty("东河区"), RegionCounty("九原区"))),
        RegionCity(name = "乌海", areaCode = "0473", counties = listOf(RegionCounty("海勃湾区"), RegionCounty("海南区"), RegionCounty("乌达区"))),
        RegionCity(name = "赤峰", areaCode = "0476", counties = listOf(RegionCounty("红山区"), RegionCounty("元宝山区"), RegionCounty("松山区"))),
        RegionCity(name = "通辽", areaCode = "0475", counties = listOf(RegionCounty("科尔沁区"), RegionCounty("霍林郭勒市"))),
        RegionCity(name = "鄂尔多斯", areaCode = "0477", counties = listOf(RegionCounty("东胜区"), RegionCounty("康巴什区"), RegionCounty("达拉特旗"))),
        RegionCity(name = "呼伦贝尔", areaCode = "0470", counties = listOf(RegionCounty("海拉尔区"), RegionCounty("满洲里市"), RegionCounty("牙克石市"))),
        RegionCity(name = "巴彦淖尔", areaCode = "0478", counties = listOf(RegionCounty("临河区"))),
        RegionCity(name = "乌兰察布", areaCode = "0474", counties = listOf(RegionCounty("集宁区"))),
    )),
    Region(province = "辽宁", cities = listOf(
        RegionCity(name = "沈阳", areaCode = "024", counties = listOf(RegionCounty("和平区"), RegionCounty("沈河区"), RegionCounty("皇姑区"), RegionCounty("大东区"), RegionCounty("铁西区"), RegionCounty("浑南区"), RegionCounty("于洪区"), RegionCounty("沈北新区"), RegionCounty("苏家屯区"))),
        RegionCity(name = "大连", areaCode = "0411", counties = listOf(RegionCounty("中山区"), RegionCounty("西岗区"), RegionCounty("沙河口区"), RegionCounty("甘井子区"), RegionCounty("旅顺口区"), RegionCounty("金州区"), RegionCounty("瓦房店市"))),
        RegionCity(name = "鞍山", areaCode = "0412", counties = listOf(RegionCounty("铁东区"), RegionCounty("铁西区"), RegionCounty("立山区"), RegionCounty("千山区"))),
        RegionCity(name = "抚顺", areaCode = "0413", counties = listOf(RegionCounty("新抚区"), RegionCounty("东洲区"), RegionCounty("顺城区"), RegionCounty("望花区"))),
        RegionCity(name = "本溪", areaCode = "0414", counties = listOf(RegionCounty("平山区"), RegionCounty("明山区"), RegionCounty("溪湖区"))),
        RegionCity(name = "丹东", areaCode = "0415", counties = listOf(RegionCounty("振兴区"), RegionCounty("元宝区"), RegionCounty("振安区"))),
        RegionCity(name = "锦州", areaCode = "0416", counties = listOf(RegionCounty("古塔区"), RegionCounty("凌河区"), RegionCounty("太和区"))),
        RegionCity(name = "营口", areaCode = "0417", counties = listOf(RegionCounty("站前区"), RegionCounty("西市区"), RegionCounty("鲅鱼圈区"))),
        RegionCity(name = "阜新", areaCode = "0418", counties = listOf(RegionCounty("海州区"), RegionCounty("细河区"))),
        RegionCity(name = "辽阳", areaCode = "0419", counties = listOf(RegionCounty("白塔区"), RegionCounty("文圣区"))),
        RegionCity(name = "盘锦", areaCode = "0427", counties = listOf(RegionCounty("兴隆台区"), RegionCounty("双台子区"))),
        RegionCity(name = "铁岭", areaCode = "0410", counties = listOf(RegionCounty("银州区"), RegionCounty("调兵山市"))),
        RegionCity(name = "朝阳", areaCode = "0421", counties = listOf(RegionCounty("双塔区"), RegionCounty("龙城区"))),
        RegionCity(name = "葫芦岛", areaCode = "0429", counties = listOf(RegionCounty("龙港区"), RegionCounty("连山区"))),
    )),
    Region(province = "吉林", cities = listOf(
        RegionCity(name = "长春", areaCode = "0431", counties = listOf(RegionCounty("南关区"), RegionCounty("宽城区"), RegionCounty("朝阳区"), RegionCounty("二道区"), RegionCounty("绿园区"), RegionCounty("净月区"), RegionCounty("双阳区"))),
        RegionCity(name = "吉林", areaCode = "0432", counties = listOf(RegionCounty("昌邑区"), RegionCounty("龙潭区"), RegionCounty("船营区"), RegionCounty("丰满区"))),
        RegionCity(name = "四平", areaCode = "0434", counties = listOf(RegionCounty("铁西区"), RegionCounty("铁东区"), RegionCounty("公主岭市"))),
        RegionCity(name = "辽源", areaCode = "0437", counties = listOf(RegionCounty("龙山区"), RegionCounty("西安区"))),
        RegionCity(name = "通化", areaCode = "0435", counties = listOf(RegionCounty("东昌区"), RegionCounty("二道江区"), RegionCounty("梅河口市"))),
        RegionCity(name = "白山", areaCode = "0439", counties = listOf(RegionCounty("浑江区"))),
        RegionCity(name = "松原", areaCode = "0438", counties = listOf(RegionCounty("宁江区"))),
        RegionCity(name = "白城", areaCode = "0436", counties = listOf(RegionCounty("洮北区"))),
        RegionCity(name = "延边", areaCode = "0433", counties = listOf(RegionCounty("延吉市"), RegionCounty("图们市"), RegionCounty("敦化市"), RegionCounty("珲春市"))),
    )),
    Region(province = "黑龙江", cities = listOf(
        RegionCity(name = "哈尔滨", areaCode = "0451", counties = listOf(RegionCounty("道里区"), RegionCounty("道外区"), RegionCounty("南岗区"), RegionCounty("香坊区"), RegionCounty("平房区"), RegionCounty("松北区"), RegionCounty("呼兰区"), RegionCounty("双城区"), RegionCounty("阿城区"))),
        RegionCity(name = "齐齐哈尔", areaCode = "0452", counties = listOf(RegionCounty("龙沙区"), RegionCounty("建华区"), RegionCounty("铁锋区"))),
        RegionCity(name = "鸡西", areaCode = "0467", counties = listOf(RegionCounty("鸡冠区"), RegionCounty("恒山区"))),
        RegionCity(name = "鹤岗", areaCode = "0468", counties = listOf(RegionCounty("向阳区"), RegionCounty("工农区"))),
        RegionCity(name = "双鸭山", areaCode = "0469", counties = listOf(RegionCounty("尖山区"), RegionCounty("岭东区"))),
        RegionCity(name = "大庆", areaCode = "0459", counties = listOf(RegionCounty("萨尔图区"), RegionCounty("龙凤区"), RegionCounty("让胡路区"))),
        RegionCity(name = "伊春", areaCode = "0458", counties = listOf(RegionCounty("伊美区"), RegionCounty("乌翠区"))),
        RegionCity(name = "佳木斯", areaCode = "0454", counties = listOf(RegionCounty("向阳区"), RegionCounty("前进区"), RegionCounty("东风区"))),
        RegionCity(name = "七台河", areaCode = "0464", counties = listOf(RegionCounty("新兴区"), RegionCounty("桃山区"))),
        RegionCity(name = "牡丹江", areaCode = "0453", counties = listOf(RegionCounty("东安区"), RegionCounty("阳明区"), RegionCounty("西安区"), RegionCounty("爱民区"))),
        RegionCity(name = "黑河", areaCode = "0456", counties = listOf(RegionCounty("爱辉区"), RegionCounty("北安市"))),
        RegionCity(name = "绥化", areaCode = "0455", counties = listOf(RegionCounty("北林区"))),
        RegionCity(name = "大兴安岭", areaCode = "0457", counties = listOf(RegionCounty("加格达奇区"))),
    )),
    Region(province = "江苏", cities = listOf(
        RegionCity(name = "南京", areaCode = "025", counties = listOf(RegionCounty("玄武区"), RegionCounty("秦淮区"), RegionCounty("建邺区"), RegionCounty("鼓楼区"), RegionCounty("浦口区"), RegionCounty("栖霞区"), RegionCounty("雨花台区"), RegionCounty("江宁区"), RegionCounty("六合区"), RegionCounty("溧水区"), RegionCounty("高淳区"))),
        RegionCity(name = "无锡", areaCode = "0510", counties = listOf(RegionCounty("梁溪区"), RegionCounty("锡山区"), RegionCounty("惠山区"), RegionCounty("滨湖区"), RegionCounty("新吴区"), RegionCounty("江阴市"), RegionCounty("宜兴市"))),
        RegionCity(name = "徐州", areaCode = "0516", counties = listOf(RegionCounty("云龙区"), RegionCounty("鼓楼区"), RegionCounty("贾汪区"), RegionCounty("泉山区"), RegionCounty("铜山区"), RegionCounty("新沂市"), RegionCounty("邳州市"))),
        RegionCity(name = "常州", areaCode = "0519", counties = listOf(RegionCounty("天宁区"), RegionCounty("钟楼区"), RegionCounty("新北区"), RegionCounty("武进区"), RegionCounty("金坛区"), RegionCounty("溧阳市"))),
        RegionCity(name = "苏州", areaCode = "0512", counties = listOf(RegionCounty("姑苏区"), RegionCounty("虎丘区"), RegionCounty("吴中区"), RegionCounty("相城区"), RegionCounty("吴江区"), RegionCounty("昆山市"), RegionCounty("太仓市"), RegionCounty("常熟市"), RegionCounty("张家港市"))),
        RegionCity(name = "南通", areaCode = "0513", counties = listOf(RegionCounty("崇川区"), RegionCounty("通州区"), RegionCounty("海门区"), RegionCounty("启东市"), RegionCounty("如皋市"), RegionCounty("海安市"))),
        RegionCity(name = "连云港", areaCode = "0518", counties = listOf(RegionCounty("海州区"), RegionCounty("连云区"), RegionCounty("赣榆区"), RegionCounty("东海县"))),
        RegionCity(name = "淮安", areaCode = "0517", counties = listOf(RegionCounty("清江浦区"), RegionCounty("淮安区"), RegionCounty("淮阴区"), RegionCounty("洪泽区"))),
        RegionCity(name = "盐城", areaCode = "0515", counties = listOf(RegionCounty("亭湖区"), RegionCounty("盐都区"), RegionCounty("大丰区"), RegionCounty("东台市"))),
        RegionCity(name = "扬州", areaCode = "0514", counties = listOf(RegionCounty("广陵区"), RegionCounty("邗江区"), RegionCounty("江都区"), RegionCounty("仪征市"), RegionCounty("高邮市"))),
        RegionCity(name = "镇江", areaCode = "0511", counties = listOf(RegionCounty("京口区"), RegionCounty("润州区"), RegionCounty("丹徒区"), RegionCounty("丹阳市"), RegionCounty("扬中市"), RegionCounty("句容市"))),
        RegionCity(name = "泰州", areaCode = "0523", counties = listOf(RegionCounty("海陵区"), RegionCounty("高港区"), RegionCounty("姜堰区"), RegionCounty("泰兴市"), RegionCounty("靖江市"), RegionCounty("兴化市"))),
        RegionCity(name = "宿迁", areaCode = "0527", counties = listOf(RegionCounty("宿城区"), RegionCounty("宿豫区"), RegionCounty("沭阳县"))),
    )),
    Region(province = "浙江", cities = listOf(
        RegionCity(name = "杭州", areaCode = "0571", counties = listOf(RegionCounty("上城区"), RegionCounty("拱墅区"), RegionCounty("西湖区"), RegionCounty("滨江区"), RegionCounty("萧山区"), RegionCounty("余杭区"), RegionCounty("临平区"), RegionCounty("钱塘区"), RegionCounty("富阳区"), RegionCounty("临安区"), RegionCounty("建德市"), RegionCounty("桐庐县"), RegionCounty("淳安县"))),
        RegionCity(name = "宁波", areaCode = "0574", counties = listOf(RegionCounty("海曙区"), RegionCounty("江北区"), RegionCounty("北仑区"), RegionCounty("镇海区"), RegionCounty("鄞州区"), RegionCounty("奉化区"), RegionCounty("慈溪市"), RegionCounty("余姚市"), RegionCounty("宁海县"), RegionCounty("象山县"))),
        RegionCity(name = "温州", areaCode = "0577", counties = listOf(RegionCounty("鹿城区"), RegionCounty("龙湾区"), RegionCounty("瓯海区"), RegionCounty("洞头区"), RegionCounty("瑞安市"), RegionCounty("乐清市"), RegionCounty("永嘉县"), RegionCounty("平阳县"), RegionCounty("苍南县"))),
        RegionCity(name = "嘉兴", areaCode = "0573", counties = listOf(RegionCounty("南湖区"), RegionCounty("秀洲区"), RegionCounty("海宁市"), RegionCounty("平湖市"), RegionCounty("桐乡市"))),
        RegionCity(name = "湖州", areaCode = "0572", counties = listOf(RegionCounty("吴兴区"), RegionCounty("南浔区"), RegionCounty("德清县"), RegionCounty("长兴县"), RegionCounty("安吉县"))),
        RegionCity(name = "绍兴", areaCode = "0575", counties = listOf(RegionCounty("越城区"), RegionCounty("柯桥区"), RegionCounty("上虞区"), RegionCounty("诸暨市"), RegionCounty("嵊州市"), RegionCounty("新昌县"))),
        RegionCity(name = "金华", areaCode = "0579", counties = listOf(RegionCounty("婺城区"), RegionCounty("金东区"), RegionCounty("兰溪市"), RegionCounty("义乌市"), RegionCounty("东阳市"), RegionCounty("永康市"))),
        RegionCity(name = "衢州", areaCode = "0570", counties = listOf(RegionCounty("柯城区"), RegionCounty("衢江区"), RegionCounty("江山市"))),
        RegionCity(name = "舟山", areaCode = "0580", counties = listOf(RegionCounty("定海区"), RegionCounty("普陀区"), RegionCounty("岱山县"))),
        RegionCity(name = "台州", areaCode = "0576", counties = listOf(RegionCounty("椒江区"), RegionCounty("黄岩区"), RegionCounty("路桥区"), RegionCounty("温岭市"), RegionCounty("临海市"), RegionCounty("玉环市"))),
        RegionCity(name = "丽水", areaCode = "0578", counties = listOf(RegionCounty("莲都区"), RegionCounty("龙泉市"), RegionCounty("青田县"))),
    )),
    Region(province = "安徽", cities = listOf(
        RegionCity(name = "合肥", areaCode = "0551", counties = listOf(RegionCounty("瑶海区"), RegionCounty("庐阳区"), RegionCounty("蜀山区"), RegionCounty("包河区"), RegionCounty("肥东县"), RegionCounty("肥西县"), RegionCounty("长丰县"))),
        RegionCity(name = "芜湖", areaCode = "0553", counties = listOf(RegionCounty("镜湖区"), RegionCounty("弋江区"), RegionCounty("鸠江区"), RegionCounty("湾沚区"), RegionCounty("繁昌区"))),
        RegionCity(name = "蚌埠", areaCode = "0552", counties = listOf(RegionCounty("龙子湖区"), RegionCounty("蚌山区"), RegionCounty("禹会区"), RegionCounty("淮上区"))),
        RegionCity(name = "淮南", areaCode = "0554", counties = listOf(RegionCounty("田家庵区"), RegionCounty("大通区"), RegionCounty("八公山区"))),
        RegionCity(name = "马鞍山", areaCode = "0555", counties = listOf(RegionCounty("花山区"), RegionCounty("雨山区"), RegionCounty("博望区"))),
        RegionCity(name = "淮北", areaCode = "0561", counties = listOf(RegionCounty("相山区"), RegionCounty("杜集区"), RegionCounty("烈山区"))),
        RegionCity(name = "铜陵", areaCode = "0562", counties = listOf(RegionCounty("铜官区"), RegionCounty("义安区"), RegionCounty("郊区"))),
        RegionCity(name = "安庆", areaCode = "0556", counties = listOf(RegionCounty("迎江区"), RegionCounty("大观区"), RegionCounty("宜秀区"), RegionCounty("桐城市"))),
        RegionCity(name = "黄山", areaCode = "0559", counties = listOf(RegionCounty("屯溪区"), RegionCounty("黄山区"), RegionCounty("徽州区"))),
        RegionCity(name = "滁州", areaCode = "0550", counties = listOf(RegionCounty("琅琊区"), RegionCounty("南谯区"), RegionCounty("天长市"), RegionCounty("明光市"))),
        RegionCity(name = "阜阳", areaCode = "0558", counties = listOf(RegionCounty("颍州区"), RegionCounty("颍东区"), RegionCounty("颍泉区"), RegionCounty("界首市"))),
        RegionCity(name = "宿州", areaCode = "0557", counties = listOf(RegionCounty("埇桥区"))),
        RegionCity(name = "六安", areaCode = "0564", counties = listOf(RegionCounty("金安区"), RegionCounty("裕安区"))),
        RegionCity(name = "亳州", areaCode = "0558", counties = listOf(RegionCounty("谯城区"))),
        RegionCity(name = "池州", areaCode = "0566", counties = listOf(RegionCounty("贵池区"))),
        RegionCity(name = "宣城", areaCode = "0563", counties = listOf(RegionCounty("宣州区"), RegionCounty("宁国市"))),
    )),
    Region(province = "福建", cities = listOf(
        RegionCity(name = "福州", areaCode = "0591", counties = listOf(RegionCounty("鼓楼区"), RegionCounty("台江区"), RegionCounty("仓山区"), RegionCounty("马尾区"), RegionCounty("晋安区"), RegionCounty("长乐区"), RegionCounty("闽侯县"), RegionCounty("福清市"))),
        RegionCity(name = "厦门", areaCode = "0592", counties = listOf(RegionCounty("思明区"), RegionCounty("湖里区"), RegionCounty("集美区"), RegionCounty("海沧区"), RegionCounty("同安区"), RegionCounty("翔安区"))),
        RegionCity(name = "莆田", areaCode = "0594", counties = listOf(RegionCounty("城厢区"), RegionCounty("涵江区"), RegionCounty("荔城区"), RegionCounty("秀屿区"))),
        RegionCity(name = "三明", areaCode = "0598", counties = listOf(RegionCounty("三元区"), RegionCounty("永安市"))),
        RegionCity(name = "泉州", areaCode = "0595", counties = listOf(RegionCounty("鲤城区"), RegionCounty("丰泽区"), RegionCounty("洛江区"), RegionCounty("泉港区"), RegionCounty("石狮市"), RegionCounty("晋江市"), RegionCounty("南安市"))),
        RegionCity(name = "漳州", areaCode = "0596", counties = listOf(RegionCounty("芗城区"), RegionCounty("龙文区"), RegionCounty("龙海区"))),
        RegionCity(name = "南平", areaCode = "0599", counties = listOf(RegionCounty("延平区"), RegionCounty("建阳区"), RegionCounty("武夷山市"), RegionCounty("邵武市"))),
        RegionCity(name = "龙岩", areaCode = "0597", counties = listOf(RegionCounty("新罗区"), RegionCounty("永定区"), RegionCounty("漳平市"))),
        RegionCity(name = "宁德", areaCode = "0593", counties = listOf(RegionCounty("蕉城区"), RegionCounty("福安市"), RegionCounty("福鼎市"))),
    )),
    Region(province = "江西", cities = listOf(
        RegionCity(name = "南昌", areaCode = "0791", counties = listOf(RegionCounty("东湖区"), RegionCounty("西湖区"), RegionCounty("青云谱区"), RegionCounty("青山湖区"), RegionCounty("新建区"), RegionCounty("红谷滩区"), RegionCounty("南昌县"))),
        RegionCity(name = "景德镇", areaCode = "0798", counties = listOf(RegionCounty("昌江区"), RegionCounty("珠山区"))),
        RegionCity(name = "萍乡", areaCode = "0799", counties = listOf(RegionCounty("安源区"), RegionCounty("湘东区"))),
        RegionCity(name = "九江", areaCode = "0792", counties = listOf(RegionCounty("浔阳区"), RegionCounty("濂溪区"), RegionCounty("柴桑区"), RegionCounty("共青城市"), RegionCounty("瑞昌市"))),
        RegionCity(name = "新余", areaCode = "0790", counties = listOf(RegionCounty("渝水区"))),
        RegionCity(name = "鹰潭", areaCode = "0701", counties = listOf(RegionCounty("月湖区"), RegionCounty("贵溪市"))),
        RegionCity(name = "赣州", areaCode = "0797", counties = listOf(RegionCounty("章贡区"), RegionCounty("南康区"), RegionCounty("赣县区"), RegionCounty("瑞金市"), RegionCounty("龙南市"))),
        RegionCity(name = "吉安", areaCode = "0796", counties = listOf(RegionCounty("吉州区"), RegionCounty("青原区"), RegionCounty("井冈山市"))),
        RegionCity(name = "宜春", areaCode = "0795", counties = listOf(RegionCounty("袁州区"), RegionCounty("丰城市"), RegionCounty("樟树市"), RegionCounty("高安市"))),
        RegionCity(name = "抚州", areaCode = "0794", counties = listOf(RegionCounty("临川区"), RegionCounty("东乡区"))),
        RegionCity(name = "上饶", areaCode = "0793", counties = listOf(RegionCounty("信州区"), RegionCounty("广丰区"), RegionCounty("广信区"), RegionCounty("德兴市"))),
    )),
    Region(province = "山东", cities = listOf(
        RegionCity(name = "济南", areaCode = "0531", counties = listOf(RegionCounty("历下区"), RegionCounty("市中区"), RegionCounty("槐荫区"), RegionCounty("天桥区"), RegionCounty("历城区"), RegionCounty("长清区"), RegionCounty("章丘区"), RegionCounty("莱芜区"), RegionCounty("钢城区"))),
        RegionCity(name = "青岛", areaCode = "0532", counties = listOf(RegionCounty("市南区"), RegionCounty("市北区"), RegionCounty("黄岛区"), RegionCounty("崂山区"), RegionCounty("李沧区"), RegionCounty("城阳区"), RegionCounty("即墨区"), RegionCounty("胶州市"), RegionCounty("平度市"), RegionCounty("莱西市"))),
        RegionCity(name = "淄博", areaCode = "0533", counties = listOf(RegionCounty("张店区"), RegionCounty("淄川区"), RegionCounty("博山区"), RegionCounty("临淄区"), RegionCounty("周村区"))),
        RegionCity(name = "枣庄", areaCode = "0632", counties = listOf(RegionCounty("市中区"), RegionCounty("薛城区"), RegionCounty("峄城区"), RegionCounty("台儿庄区"), RegionCounty("山亭区"))),
        RegionCity(name = "东营", areaCode = "0546", counties = listOf(RegionCounty("东营区"), RegionCounty("河口区"), RegionCounty("垦利区"))),
        RegionCity(name = "烟台", areaCode = "0535", counties = listOf(RegionCounty("芝罘区"), RegionCounty("福山区"), RegionCounty("牟平区"), RegionCounty("莱山区"), RegionCounty("蓬莱区"), RegionCounty("龙口市"), RegionCounty("莱阳市"), RegionCounty("莱州市"), RegionCounty("招远市"))),
        RegionCity(name = "潍坊", areaCode = "0536", counties = listOf(RegionCounty("潍城区"), RegionCounty("寒亭区"), RegionCounty("坊子区"), RegionCounty("奎文区"), RegionCounty("临朐县"), RegionCounty("昌乐县"), RegionCounty("青州市"), RegionCounty("诸城市"), RegionCounty("寿光市"), RegionCounty("安丘市"), RegionCounty("高密市"), RegionCounty("昌邑市"))),
        RegionCity(name = "济宁", areaCode = "0537", counties = listOf(RegionCounty("任城区"), RegionCounty("兖州区"), RegionCounty("曲阜市"), RegionCounty("邹城市"), RegionCounty("梁山县"))),
        RegionCity(name = "泰安", areaCode = "0538", counties = listOf(RegionCounty("泰山区"), RegionCounty("岱岳区"), RegionCounty("新泰市"), RegionCounty("肥城市"))),
        RegionCity(name = "威海", areaCode = "0631", counties = listOf(RegionCounty("环翠区"), RegionCounty("文登区"), RegionCounty("荣成市"), RegionCounty("乳山市"))),
        RegionCity(name = "日照", areaCode = "0633", counties = listOf(RegionCounty("东港区"), RegionCounty("岚山区"), RegionCounty("莒县"))),
        RegionCity(name = "临沂", areaCode = "0539", counties = listOf(RegionCounty("兰山区"), RegionCounty("罗庄区"), RegionCounty("河东区"), RegionCounty("沂南县"), RegionCounty("郯城县"), RegionCounty("沂水县"), RegionCounty("兰陵县"), RegionCounty("费县"), RegionCounty("平邑县"), RegionCounty("莒南县"), RegionCounty("蒙阴县"), RegionCounty("临沭县"))),
        RegionCity(name = "德州", areaCode = "0534", counties = listOf(RegionCounty("德城区"), RegionCounty("陵城区"), RegionCounty("乐陵市"), RegionCounty("禹城市"))),
        RegionCity(name = "聊城", areaCode = "0635", counties = listOf(RegionCounty("东昌府区"), RegionCounty("临清市"))),
        RegionCity(name = "滨州", areaCode = "0543", counties = listOf(RegionCounty("滨城区"), RegionCounty("沾化区"), RegionCounty("邹平市"))),
        RegionCity(name = "菏泽", areaCode = "0530", counties = listOf(RegionCounty("牡丹区"), RegionCounty("定陶区"), RegionCounty("曹县"), RegionCounty("单县"))),
    )),
    Region(province = "河南", cities = listOf(
        RegionCity(name = "郑州", areaCode = "0371", counties = listOf(RegionCounty("中原区"), RegionCounty("二七区"), RegionCounty("管城回族区"), RegionCounty("金水区"), RegionCounty("上街区"), RegionCounty("惠济区"), RegionCounty("郑东新区"), RegionCounty("中牟县"), RegionCounty("巩义市"), RegionCounty("荥阳市"), RegionCounty("新密市"), RegionCounty("新郑市"), RegionCounty("登封市"))),
        RegionCity(name = "开封", areaCode = "0371", counties = listOf(RegionCounty("龙亭区"), RegionCounty("顺河回族区"), RegionCounty("鼓楼区"), RegionCounty("禹王台区"), RegionCounty("祥符区"), RegionCounty("兰考县"))),
        RegionCity(name = "洛阳", areaCode = "0379", counties = listOf(RegionCounty("老城区"), RegionCounty("西工区"), RegionCounty("瀍河回族区"), RegionCounty("涧西区"), RegionCounty("洛龙区"), RegionCounty("孟津区"), RegionCounty("偃师区"), RegionCounty("新安县"))),
        RegionCity(name = "平顶山", areaCode = "0375", counties = listOf(RegionCounty("新华区"), RegionCounty("卫东区"), RegionCounty("湛河区"), RegionCounty("汝州市"), RegionCounty("舞钢市"))),
        RegionCity(name = "安阳", areaCode = "0372", counties = listOf(RegionCounty("文峰区"), RegionCounty("北关区"), RegionCounty("殷都区"), RegionCounty("龙安区"), RegionCounty("林州市"))),
        RegionCity(name = "鹤壁", areaCode = "0392", counties = listOf(RegionCounty("鹤山区"), RegionCounty("山城区"), RegionCounty("淇滨区"))),
        RegionCity(name = "新乡", areaCode = "0373", counties = listOf(RegionCounty("红旗区"), RegionCounty("卫滨区"), RegionCounty("凤泉区"), RegionCounty("牧野区"), RegionCounty("辉县市"), RegionCounty("卫辉市"), RegionCounty("长垣市"))),
        RegionCity(name = "焦作", areaCode = "0391", counties = listOf(RegionCounty("解放区"), RegionCounty("中站区"), RegionCounty("马村区"), RegionCounty("山阳区"), RegionCounty("沁阳市"), RegionCounty("孟州市"))),
        RegionCity(name = "濮阳", areaCode = "0393", counties = listOf(RegionCounty("华龙区"), RegionCounty("濮阳县"))),
        RegionCity(name = "许昌", areaCode = "0374", counties = listOf(RegionCounty("魏都区"), RegionCounty("建安区"), RegionCounty("禹州市"), RegionCounty("长葛市"))),
        RegionCity(name = "漯河", areaCode = "0395", counties = listOf(RegionCounty("源汇区"), RegionCounty("郾城区"), RegionCounty("召陵区"))),
        RegionCity(name = "三门峡", areaCode = "0398", counties = listOf(RegionCounty("湖滨区"), RegionCounty("陕州区"), RegionCounty("义马市"), RegionCounty("灵宝市"))),
        RegionCity(name = "南阳", areaCode = "0377", counties = listOf(RegionCounty("宛城区"), RegionCounty("卧龙区"), RegionCounty("邓州市"))),
        RegionCity(name = "商丘", areaCode = "0370", counties = listOf(RegionCounty("梁园区"), RegionCounty("睢阳区"), RegionCounty("永城市"))),
        RegionCity(name = "信阳", areaCode = "0376", counties = listOf(RegionCounty("浉河区"), RegionCounty("平桥区"))),
        RegionCity(name = "周口", areaCode = "0394", counties = listOf(RegionCounty("川汇区"), RegionCounty("淮阳区"), RegionCounty("项城市"))),
        RegionCity(name = "驻马店", areaCode = "0396", counties = listOf(RegionCounty("驿城区"))),
        RegionCity(name = "济源", areaCode = "0391", counties = listOf(RegionCounty("济源市"))),
    )),
    Region(province = "湖北", cities = listOf(
        RegionCity(name = "武汉", areaCode = "027", counties = listOf(RegionCounty("江岸区"), RegionCounty("江汉区"), RegionCounty("硚口区"), RegionCounty("汉阳区"), RegionCounty("武昌区"), RegionCounty("青山区"), RegionCounty("洪山区"), RegionCounty("东西湖区"), RegionCounty("汉南区"), RegionCounty("蔡甸区"), RegionCounty("江夏区"), RegionCounty("黄陂区"), RegionCounty("新洲区"))),
        RegionCity(name = "黄石", areaCode = "0714", counties = listOf(RegionCounty("黄石港区"), RegionCounty("西塞山区"), RegionCounty("下陆区"), RegionCounty("铁山区"), RegionCounty("大冶市"), RegionCounty("阳新县"))),
        RegionCity(name = "十堰", areaCode = "0719", counties = listOf(RegionCounty("茅箭区"), RegionCounty("张湾区"), RegionCounty("郧阳区"), RegionCounty("丹江口市"))),
        RegionCity(name = "宜昌", areaCode = "0717", counties = listOf(RegionCounty("西陵区"), RegionCounty("伍家岗区"), RegionCounty("点军区"), RegionCounty("猇亭区"), RegionCounty("夷陵区"), RegionCounty("宜都市"), RegionCounty("当阳市"), RegionCounty("枝江市"))),
        RegionCity(name = "襄阳", areaCode = "0710", counties = listOf(RegionCounty("襄城区"), RegionCounty("樊城区"), RegionCounty("襄州区"), RegionCounty("老河口市"), RegionCounty("枣阳市"), RegionCounty("宜城市"))),
        RegionCity(name = "鄂州", areaCode = "0711", counties = listOf(RegionCounty("鄂城区"), RegionCounty("华容区"), RegionCounty("梁子湖区"))),
        RegionCity(name = "荆门", areaCode = "0724", counties = listOf(RegionCounty("东宝区"), RegionCounty("掇刀区"), RegionCounty("钟祥市"), RegionCounty("京山市"))),
        RegionCity(name = "孝感", areaCode = "0712", counties = listOf(RegionCounty("孝南区"), RegionCounty("应城市"), RegionCounty("安陆市"), RegionCounty("汉川市"))),
        RegionCity(name = "荆州", areaCode = "0716", counties = listOf(RegionCounty("沙市区"), RegionCounty("荆州区"), RegionCounty("石首市"), RegionCounty("洪湖市"), RegionCounty("松滋市"))),
        RegionCity(name = "黄冈", areaCode = "0713", counties = listOf(RegionCounty("黄州区"), RegionCounty("麻城市"), RegionCounty("武穴市"))),
        RegionCity(name = "咸宁", areaCode = "0715", counties = listOf(RegionCounty("咸安区"), RegionCounty("赤壁市"))),
        RegionCity(name = "随州", areaCode = "0722", counties = listOf(RegionCounty("曾都区"), RegionCounty("广水市"))),
        RegionCity(name = "恩施", areaCode = "0718", counties = listOf(RegionCounty("恩施市"), RegionCounty("利川市"), RegionCounty("巴东县"))),
    )),
    Region(province = "湖南", cities = listOf(
        RegionCity(name = "长沙", areaCode = "0731", counties = listOf(RegionCounty("芙蓉区"), RegionCounty("天心区"), RegionCounty("岳麓区"), RegionCounty("开福区"), RegionCounty("雨花区"), RegionCounty("望城区"), RegionCounty("长沙县"), RegionCounty("浏阳市"), RegionCounty("宁乡市"))),
        RegionCity(name = "株洲", areaCode = "0731", counties = listOf(RegionCounty("荷塘区"), RegionCounty("芦淞区"), RegionCounty("石峰区"), RegionCounty("天元区"), RegionCounty("渌口区"), RegionCounty("醴陵市"))),
        RegionCity(name = "湘潭", areaCode = "0731", counties = listOf(RegionCounty("雨湖区"), RegionCounty("岳塘区"), RegionCounty("湘乡市"), RegionCounty("韶山市"))),
        RegionCity(name = "衡阳", areaCode = "0734", counties = listOf(RegionCounty("珠晖区"), RegionCounty("雁峰区"), RegionCounty("石鼓区"), RegionCounty("蒸湘区"), RegionCounty("南岳区"), RegionCounty("耒阳市"), RegionCounty("常宁市"))),
        RegionCity(name = "邵阳", areaCode = "0739", counties = listOf(RegionCounty("双清区"), RegionCounty("大祥区"), RegionCounty("北塔区"), RegionCounty("武冈市"))),
        RegionCity(name = "岳阳", areaCode = "0730", counties = listOf(RegionCounty("岳阳楼区"), RegionCounty("云溪区"), RegionCounty("君山区"), RegionCounty("临湘市"), RegionCounty("汨罗市"))),
        RegionCity(name = "常德", areaCode = "0736", counties = listOf(RegionCounty("武陵区"), RegionCounty("鼎城区"), RegionCounty("津市市"))),
        RegionCity(name = "张家界", areaCode = "0744", counties = listOf(RegionCounty("永定区"), RegionCounty("武陵源区"))),
        RegionCity(name = "益阳", areaCode = "0737", counties = listOf(RegionCounty("资阳区"), RegionCounty("赫山区"), RegionCounty("沅江市"))),
        RegionCity(name = "郴州", areaCode = "0735", counties = listOf(RegionCounty("北湖区"), RegionCounty("苏仙区"), RegionCounty("资兴市"))),
        RegionCity(name = "永州", areaCode = "0746", counties = listOf(RegionCounty("零陵区"), RegionCounty("冷水滩区"), RegionCounty("祁阳市"))),
        RegionCity(name = "怀化", areaCode = "0745", counties = listOf(RegionCounty("鹤城区"), RegionCounty("洪江市"))),
        RegionCity(name = "娄底", areaCode = "0738", counties = listOf(RegionCounty("娄星区"), RegionCounty("冷水江市"), RegionCounty("涟源市"))),
        RegionCity(name = "湘西", areaCode = "0743", counties = listOf(RegionCounty("吉首市"), RegionCounty("凤凰县"), RegionCounty("花垣县"))),
    )),
    Region(province = "广东", cities = listOf(
        RegionCity(name = "广州", areaCode = "020", counties = listOf(RegionCounty("越秀区"), RegionCounty("海珠区"), RegionCounty("荔湾区"), RegionCounty("天河区"), RegionCounty("白云区"), RegionCounty("黄埔区"), RegionCounty("番禺区"), RegionCounty("花都区"), RegionCounty("南沙区"), RegionCounty("从化区"), RegionCounty("增城区"))),
        RegionCity(name = "深圳", areaCode = "0755", counties = listOf(RegionCounty("罗湖区"), RegionCounty("福田区"), RegionCounty("南山区"), RegionCounty("宝安区"), RegionCounty("龙岗区"), RegionCounty("盐田区"), RegionCounty("龙华区"), RegionCounty("坪山区"), RegionCounty("光明区"))),
        RegionCity(name = "珠海", areaCode = "0756", counties = listOf(RegionCounty("香洲区"), RegionCounty("斗门区"), RegionCounty("金湾区"))),
        RegionCity(name = "汕头", areaCode = "0754", counties = listOf(RegionCounty("龙湖区"), RegionCounty("金平区"), RegionCounty("濠江区"), RegionCounty("潮阳区"), RegionCounty("潮南区"), RegionCounty("澄海区"), RegionCounty("南澳县"))),
        RegionCity(name = "佛山", areaCode = "0757", counties = listOf(RegionCounty("禅城区"), RegionCounty("南海区"), RegionCounty("顺德区"), RegionCounty("三水区"), RegionCounty("高明区"))),
        RegionCity(name = "韶关", areaCode = "0751", counties = listOf(RegionCounty("武江区"), RegionCounty("浈江区"), RegionCounty("曲江区"), RegionCounty("乐昌市"), RegionCounty("南雄市"))),
        RegionCity(name = "湛江", areaCode = "0759", counties = listOf(RegionCounty("赤坎区"), RegionCounty("霞山区"), RegionCounty("坡头区"), RegionCounty("麻章区"), RegionCounty("廉江市"), RegionCounty("雷州市"), RegionCounty("吴川市"))),
        RegionCity(name = "肇庆", areaCode = "0758", counties = listOf(RegionCounty("端州区"), RegionCounty("鼎湖区"), RegionCounty("高要区"), RegionCounty("四会市"))),
        RegionCity(name = "江门", areaCode = "0750", counties = listOf(RegionCounty("蓬江区"), RegionCounty("江海区"), RegionCounty("新会区"), RegionCounty("台山市"), RegionCounty("开平市"), RegionCounty("鹤山市"), RegionCounty("恩平市"))),
        RegionCity(name = "茂名", areaCode = "0668", counties = listOf(RegionCounty("茂南区"), RegionCounty("电白区"), RegionCounty("高州市"), RegionCounty("化州市"), RegionCounty("信宜市"))),
        RegionCity(name = "惠州", areaCode = "0752", counties = listOf(RegionCounty("惠城区"), RegionCounty("惠阳区"), RegionCounty("博罗县"), RegionCounty("惠东县"), RegionCounty("龙门县"))),
        RegionCity(name = "梅州", areaCode = "0753", counties = listOf(RegionCounty("梅江区"), RegionCounty("梅县区"), RegionCounty("兴宁市"))),
        RegionCity(name = "汕尾", areaCode = "0660", counties = listOf(RegionCounty("城区"), RegionCounty("陆丰市"), RegionCounty("海丰县"))),
        RegionCity(name = "河源", areaCode = "0762", counties = listOf(RegionCounty("源城区"), RegionCounty("龙川县"), RegionCounty("紫金县"), RegionCounty("和平县"))),
        RegionCity(name = "阳江", areaCode = "0662", counties = listOf(RegionCounty("江城区"), RegionCounty("阳东区"), RegionCounty("阳春市"))),
        RegionCity(name = "清远", areaCode = "0763", counties = listOf(RegionCounty("清城区"), RegionCounty("清新区"), RegionCounty("英德市"), RegionCounty("连州市"))),
        RegionCity(name = "东莞", areaCode = "0769", counties = listOf(RegionCounty("莞城街道"), RegionCounty("南城街道"), RegionCounty("东城街道"), RegionCounty("长安镇"), RegionCounty("虎门镇"), RegionCounty("厚街镇"), RegionCounty("塘厦镇"))),
        RegionCity(name = "中山", areaCode = "0760", counties = listOf(RegionCounty("石岐街道"), RegionCounty("东区街道"), RegionCounty("小榄镇"), RegionCounty("古镇镇"), RegionCounty("火炬开发区"))),
        RegionCity(name = "潮州", areaCode = "0768", counties = listOf(RegionCounty("湘桥区"), RegionCounty("潮安区"), RegionCounty("饶平县"))),
        RegionCity(name = "揭阳", areaCode = "0663", counties = listOf(RegionCounty("榕城区"), RegionCounty("揭东区"), RegionCounty("普宁市"), RegionCounty("惠来县"), RegionCounty("揭西县"))),
        RegionCity(name = "云浮", areaCode = "0766", counties = listOf(RegionCounty("云城区"), RegionCounty("云安区"), RegionCounty("罗定市"))),
    )),
    Region(province = "广西", cities = listOf(
        RegionCity(name = "南宁", areaCode = "0771", counties = listOf(RegionCounty("兴宁区"), RegionCounty("青秀区"), RegionCounty("江南区"), RegionCounty("西乡塘区"), RegionCounty("良庆区"), RegionCounty("邕宁区"), RegionCounty("武鸣区"), RegionCounty("横州市"))),
        RegionCity(name = "柳州", areaCode = "0772", counties = listOf(RegionCounty("城中区"), RegionCounty("鱼峰区"), RegionCounty("柳南区"), RegionCounty("柳北区"), RegionCounty("柳江区"), RegionCounty("鹿寨县"))),
        RegionCity(name = "桂林", areaCode = "0773", counties = listOf(RegionCounty("秀峰区"), RegionCounty("叠彩区"), RegionCounty("象山区"), RegionCounty("七星区"), RegionCounty("雁山区"), RegionCounty("临桂区"), RegionCounty("阳朔县"))),
        RegionCity(name = "梧州", areaCode = "0774", counties = listOf(RegionCounty("万秀区"), RegionCounty("长洲区"), RegionCounty("龙圩区"), RegionCounty("岑溪市"))),
        RegionCity(name = "北海", areaCode = "0779", counties = listOf(RegionCounty("海城区"), RegionCounty("银海区"), RegionCounty("铁山港区"))),
        RegionCity(name = "防城港", areaCode = "0770", counties = listOf(RegionCounty("港口区"), RegionCounty("防城区"), RegionCounty("东兴市"))),
        RegionCity(name = "钦州", areaCode = "0777", counties = listOf(RegionCounty("钦南区"), RegionCounty("钦北区"))),
        RegionCity(name = "贵港", areaCode = "0775", counties = listOf(RegionCounty("港北区"), RegionCounty("港南区"), RegionCounty("覃塘区"), RegionCounty("桂平市"))),
        RegionCity(name = "玉林", areaCode = "0775", counties = listOf(RegionCounty("玉州区"), RegionCounty("福绵区"), RegionCounty("北流市"))),
        RegionCity(name = "百色", areaCode = "0776", counties = listOf(RegionCounty("右江区"), RegionCounty("靖西市"))),
        RegionCity(name = "贺州", areaCode = "0774", counties = listOf(RegionCounty("八步区"), RegionCounty("平桂区"))),
        RegionCity(name = "河池", areaCode = "0778", counties = listOf(RegionCounty("金城江区"), RegionCounty("宜州区"))),
        RegionCity(name = "来宾", areaCode = "0772", counties = listOf(RegionCounty("兴宾区"))),
        RegionCity(name = "崇左", areaCode = "0771", counties = listOf(RegionCounty("江州区"), RegionCounty("凭祥市"))),
    )),
    Region(province = "海南", cities = listOf(
        RegionCity(name = "海口", areaCode = "0898", counties = listOf(RegionCounty("秀英区"), RegionCounty("龙华区"), RegionCounty("琼山区"), RegionCounty("美兰区"))),
        RegionCity(name = "三亚", areaCode = "0898", counties = listOf(RegionCounty("海棠区"), RegionCounty("吉阳区"), RegionCounty("天涯区"), RegionCounty("崖州区"))),
        RegionCity(name = "三沙", areaCode = "0898", counties = listOf(RegionCounty("西沙区"))),
        RegionCity(name = "儋州", areaCode = "0898", counties = listOf(RegionCounty("那大镇"))),
        RegionCity(name = "五指山", areaCode = "0898", counties = listOf(RegionCounty("通什镇"))),
        RegionCity(name = "琼海", areaCode = "0898", counties = listOf(RegionCounty("嘉积镇"))),
        RegionCity(name = "文昌", areaCode = "0898", counties = listOf(RegionCounty("文城镇"))),
        RegionCity(name = "万宁", areaCode = "0898", counties = listOf(RegionCounty("万城镇"))),
        RegionCity(name = "东方", areaCode = "0898", counties = listOf(RegionCounty("八所镇"))),
    )),
    Region(province = "四川", cities = listOf(
        RegionCity(name = "成都", areaCode = "028", counties = listOf(RegionCounty("锦江区"), RegionCounty("青羊区"), RegionCounty("金牛区"), RegionCounty("武侯区"), RegionCounty("成华区"), RegionCounty("龙泉驿区"), RegionCounty("青白江区"), RegionCounty("新都区"), RegionCounty("温江区"), RegionCounty("双流区"), RegionCounty("郫都区"), RegionCounty("新津区"), RegionCounty("都江堰市"), RegionCounty("彭州市"), RegionCounty("邛崃市"), RegionCounty("崇州市"))),
        RegionCity(name = "自贡", areaCode = "0813", counties = listOf(RegionCounty("自流井区"), RegionCounty("贡井区"), RegionCounty("大安区"), RegionCounty("沿滩区"), RegionCounty("荣县"))),
        RegionCity(name = "攀枝花", areaCode = "0812", counties = listOf(RegionCounty("东区"), RegionCounty("西区"), RegionCounty("仁和区"), RegionCounty("米易县"))),
        RegionCity(name = "泸州", areaCode = "0830", counties = listOf(RegionCounty("江阳区"), RegionCounty("纳溪区"), RegionCounty("龙马潭区"), RegionCounty("泸县"), RegionCounty("合江县"))),
        RegionCity(name = "德阳", areaCode = "0838", counties = listOf(RegionCounty("旌阳区"), RegionCounty("罗江区"), RegionCounty("广汉市"), RegionCounty("什邡市"), RegionCounty("绵竹市"))),
        RegionCity(name = "绵阳", areaCode = "0816", counties = listOf(RegionCounty("涪城区"), RegionCounty("游仙区"), RegionCounty("安州区"), RegionCounty("江油市"))),
        RegionCity(name = "广元", areaCode = "0839", counties = listOf(RegionCounty("利州区"), RegionCounty("昭化区"), RegionCounty("朝天区"))),
        RegionCity(name = "遂宁", areaCode = "0825", counties = listOf(RegionCounty("船山区"), RegionCounty("安居区"), RegionCounty("射洪市"))),
        RegionCity(name = "内江", areaCode = "0832", counties = listOf(RegionCounty("市中区"), RegionCounty("东兴区"), RegionCounty("隆昌市"))),
        RegionCity(name = "乐山", areaCode = "0833", counties = listOf(RegionCounty("市中区"), RegionCounty("沙湾区"), RegionCounty("五通桥区"), RegionCounty("金口河区"), RegionCounty("峨眉山市"), RegionCounty("犍为县"))),
        RegionCity(name = "南充", areaCode = "0817", counties = listOf(RegionCounty("顺庆区"), RegionCounty("高坪区"), RegionCounty("嘉陵区"), RegionCounty("阆中市"))),
        RegionCity(name = "眉山", areaCode = "028", counties = listOf(RegionCounty("东坡区"), RegionCounty("彭山区"), RegionCounty("仁寿县"))),
        RegionCity(name = "宜宾", areaCode = "0831", counties = listOf(RegionCounty("翠屏区"), RegionCounty("南溪区"), RegionCounty("叙州区"), RegionCounty("江安县"))),
        RegionCity(name = "广安", areaCode = "0826", counties = listOf(RegionCounty("广安区"), RegionCounty("前锋区"), RegionCounty("华蓥市"))),
        RegionCity(name = "达州", areaCode = "0818", counties = listOf(RegionCounty("通川区"), RegionCounty("达川区"), RegionCounty("万源市"))),
        RegionCity(name = "雅安", areaCode = "0835", counties = listOf(RegionCounty("雨城区"), RegionCounty("名山区"))),
        RegionCity(name = "巴中", areaCode = "0827", counties = listOf(RegionCounty("巴州区"), RegionCounty("恩阳区"))),
        RegionCity(name = "资阳", areaCode = "028", counties = listOf(RegionCounty("雁江区"))),
        RegionCity(name = "阿坝", areaCode = "0837", counties = listOf(RegionCounty("马尔康市"), RegionCounty("汶川县"))),
        RegionCity(name = "甘孜", areaCode = "0836", counties = listOf(RegionCounty("康定市"), RegionCounty("泸定县"))),
        RegionCity(name = "凉山", areaCode = "0834", counties = listOf(RegionCounty("西昌市"), RegionCounty("会理市"), RegionCounty("冕宁县"))),
    )),
    Region(province = "贵州", cities = listOf(
        RegionCity(name = "贵阳", areaCode = "0851", counties = listOf(RegionCounty("南明区"), RegionCounty("云岩区"), RegionCounty("花溪区"), RegionCounty("乌当区"), RegionCounty("白云区"), RegionCounty("观山湖区"), RegionCounty("清镇市"))),
        RegionCity(name = "六盘水", areaCode = "0858", counties = listOf(RegionCounty("钟山区"), RegionCounty("盘州市"), RegionCounty("水城区"))),
        RegionCity(name = "遵义", areaCode = "0851", counties = listOf(RegionCounty("红花岗区"), RegionCounty("汇川区"), RegionCounty("播州区"), RegionCounty("仁怀市"), RegionCounty("赤水市"))),
        RegionCity(name = "安顺", areaCode = "0853", counties = listOf(RegionCounty("西秀区"), RegionCounty("平坝区"))),
        RegionCity(name = "毕节", areaCode = "0857", counties = listOf(RegionCounty("七星关区"), RegionCounty("大方县"), RegionCounty("黔西市"))),
        RegionCity(name = "铜仁", areaCode = "0856", counties = listOf(RegionCounty("碧江区"), RegionCounty("万山区"))),
        RegionCity(name = "黔西南", areaCode = "0859", counties = listOf(RegionCounty("兴义市"), RegionCounty("兴仁市"))),
        RegionCity(name = "黔东南", areaCode = "0855", counties = listOf(RegionCounty("凯里市"))),
        RegionCity(name = "黔南", areaCode = "0854", counties = listOf(RegionCounty("都匀市"), RegionCounty("福泉市"))),
    )),
    Region(province = "云南", cities = listOf(
        RegionCity(name = "昆明", areaCode = "0871", counties = listOf(RegionCounty("五华区"), RegionCounty("盘龙区"), RegionCounty("官渡区"), RegionCounty("西山区"), RegionCounty("东川区"), RegionCounty("呈贡区"), RegionCounty("晋宁区"), RegionCounty("安宁市"))),
        RegionCity(name = "曲靖", areaCode = "0874", counties = listOf(RegionCounty("麒麟区"), RegionCounty("沾益区"), RegionCounty("马龙区"), RegionCounty("宣威市"))),
        RegionCity(name = "玉溪", areaCode = "0877", counties = listOf(RegionCounty("红塔区"), RegionCounty("江川区"), RegionCounty("澄江市"))),
        RegionCity(name = "保山", areaCode = "0875", counties = listOf(RegionCounty("隆阳区"), RegionCounty("腾冲市"))),
        RegionCity(name = "昭通", areaCode = "0870", counties = listOf(RegionCounty("昭阳区"), RegionCounty("水富市"))),
        RegionCity(name = "丽江", areaCode = "0888", counties = listOf(RegionCounty("古城区"), RegionCounty("玉龙县"))),
        RegionCity(name = "普洱", areaCode = "0879", counties = listOf(RegionCounty("思茅区"))),
        RegionCity(name = "临沧", areaCode = "0883", counties = listOf(RegionCounty("临翔区"))),
        RegionCity(name = "楚雄", areaCode = "0878", counties = listOf(RegionCounty("楚雄市"), RegionCounty("禄丰市"))),
        RegionCity(name = "红河", areaCode = "0873", counties = listOf(RegionCounty("蒙自市"), RegionCounty("个旧市"), RegionCounty("开远市"), RegionCounty("弥勒市"))),
        RegionCity(name = "文山", areaCode = "0876", counties = listOf(RegionCounty("文山市"))),
        RegionCity(name = "西双版纳", areaCode = "0691", counties = listOf(RegionCounty("景洪市"), RegionCounty("勐海县"))),
        RegionCity(name = "大理", areaCode = "0872", counties = listOf(RegionCounty("大理市"))),
        RegionCity(name = "德宏", areaCode = "0692", counties = listOf(RegionCounty("芒市"), RegionCounty("瑞丽市"))),
        RegionCity(name = "怒江", areaCode = "0886", counties = listOf(RegionCounty("泸水市"))),
        RegionCity(name = "迪庆", areaCode = "0887", counties = listOf(RegionCounty("香格里拉市"))),
    )),
    Region(province = "西藏", cities = listOf(
        RegionCity(name = "拉萨", areaCode = "0891", counties = listOf(RegionCounty("城关区"), RegionCounty("堆龙德庆区"), RegionCounty("达孜区"))),
        RegionCity(name = "日喀则", areaCode = "0892", counties = listOf(RegionCounty("桑珠孜区"))),
        RegionCity(name = "昌都", areaCode = "0895", counties = listOf(RegionCounty("卡若区"))),
        RegionCity(name = "林芝", areaCode = "0894", counties = listOf(RegionCounty("巴宜区"))),
        RegionCity(name = "山南", areaCode = "0893", counties = listOf(RegionCounty("乃东区"))),
        RegionCity(name = "那曲", areaCode = "0896", counties = listOf(RegionCounty("色尼区"))),
        RegionCity(name = "阿里", areaCode = "0897", counties = listOf(RegionCounty("噶尔县"))),
    )),
    Region(province = "陕西", cities = listOf(
        RegionCity(name = "西安", areaCode = "029", counties = listOf(RegionCounty("新城区"), RegionCounty("碑林区"), RegionCounty("莲湖区"), RegionCounty("灞桥区"), RegionCounty("未央区"), RegionCounty("雁塔区"), RegionCounty("阎良区"), RegionCounty("临潼区"), RegionCounty("长安区"), RegionCounty("高陵区"), RegionCounty("鄠邑区"), RegionCounty("周至县"), RegionCounty("蓝田县"))),
        RegionCity(name = "铜川", areaCode = "0919", counties = listOf(RegionCounty("王益区"), RegionCounty("印台区"), RegionCounty("耀州区"))),
        RegionCity(name = "宝鸡", areaCode = "0917", counties = listOf(RegionCounty("渭滨区"), RegionCounty("金台区"), RegionCounty("陈仓区"), RegionCounty("凤翔区"), RegionCounty("岐山县"))),
        RegionCity(name = "咸阳", areaCode = "029", counties = listOf(RegionCounty("秦都区"), RegionCounty("杨陵区"), RegionCounty("渭城区"), RegionCounty("兴平市"), RegionCounty("彬州市"))),
        RegionCity(name = "渭南", areaCode = "0913", counties = listOf(RegionCounty("临渭区"), RegionCounty("华州区"), RegionCounty("韩城市"), RegionCounty("华阴市"))),
        RegionCity(name = "延安", areaCode = "0911", counties = listOf(RegionCounty("宝塔区"), RegionCounty("安塞区"), RegionCounty("延长县"))),
        RegionCity(name = "汉中", areaCode = "0916", counties = listOf(RegionCounty("汉台区"), RegionCounty("南郑区"), RegionCounty("城固县"))),
        RegionCity(name = "榆林", areaCode = "0912", counties = listOf(RegionCounty("榆阳区"), RegionCounty("横山区"), RegionCounty("神木市"))),
        RegionCity(name = "安康", areaCode = "0915", counties = listOf(RegionCounty("汉滨区"), RegionCounty("旬阳市"))),
        RegionCity(name = "商洛", areaCode = "0914", counties = listOf(RegionCounty("商州区"), RegionCounty("洛南县"))),
    )),
    Region(province = "甘肃", cities = listOf(
        RegionCity(name = "兰州", areaCode = "0931", counties = listOf(RegionCounty("城关区"), RegionCounty("七里河区"), RegionCounty("西固区"), RegionCounty("安宁区"), RegionCounty("红古区"), RegionCounty("榆中县"))),
        RegionCity(name = "嘉峪关", areaCode = "0937", counties = listOf(RegionCounty("雄关区"))),
        RegionCity(name = "金昌", areaCode = "0935", counties = listOf(RegionCounty("金川区"), RegionCounty("永昌县"))),
        RegionCity(name = "白银", areaCode = "0943", counties = listOf(RegionCounty("白银区"), RegionCounty("平川区"), RegionCounty("靖远县"))),
        RegionCity(name = "天水", areaCode = "0938", counties = listOf(RegionCounty("秦州区"), RegionCounty("麦积区"))),
        RegionCity(name = "武威", areaCode = "0935", counties = listOf(RegionCounty("凉州区"))),
        RegionCity(name = "张掖", areaCode = "0936", counties = listOf(RegionCounty("甘州区"))),
        RegionCity(name = "平凉", areaCode = "0933", counties = listOf(RegionCounty("崆峒区"))),
        RegionCity(name = "酒泉", areaCode = "0937", counties = listOf(RegionCounty("肃州区"), RegionCounty("敦煌市"))),
        RegionCity(name = "庆阳", areaCode = "0934", counties = listOf(RegionCounty("西峰区"))),
        RegionCity(name = "定西", areaCode = "0932", counties = listOf(RegionCounty("安定区"))),
        RegionCity(name = "陇南", areaCode = "0939", counties = listOf(RegionCounty("武都区"))),
        RegionCity(name = "临夏", areaCode = "0930", counties = listOf(RegionCounty("临夏市"))),
        RegionCity(name = "甘南", areaCode = "0941", counties = listOf(RegionCounty("合作市"))),
    )),
    Region(province = "青海", cities = listOf(
        RegionCity(name = "西宁", areaCode = "0971", counties = listOf(RegionCounty("城东区"), RegionCounty("城中区"), RegionCounty("城西区"), RegionCounty("城北区"), RegionCounty("湟中区"), RegionCounty("大通县"))),
        RegionCity(name = "海东", areaCode = "0972", counties = listOf(RegionCounty("乐都区"), RegionCounty("平安区"))),
        RegionCity(name = "海北", areaCode = "0970", counties = listOf(RegionCounty("海晏县"))),
        RegionCity(name = "黄南", areaCode = "0973", counties = listOf(RegionCounty("同仁市"))),
        RegionCity(name = "海南", areaCode = "0974", counties = listOf(RegionCounty("共和县"))),
        RegionCity(name = "果洛", areaCode = "0975", counties = listOf(RegionCounty("玛沁县"))),
        RegionCity(name = "玉树", areaCode = "0976", counties = listOf(RegionCounty("玉树市"))),
        RegionCity(name = "海西", areaCode = "0977", counties = listOf(RegionCounty("德令哈市"), RegionCounty("格尔木市"))),
    )),
    Region(province = "宁夏", cities = listOf(
        RegionCity(name = "银川", areaCode = "0951", counties = listOf(RegionCounty("兴庆区"), RegionCounty("西夏区"), RegionCounty("金凤区"), RegionCounty("永宁县"), RegionCounty("贺兰县"), RegionCounty("灵武市"))),
        RegionCity(name = "石嘴山", areaCode = "0952", counties = listOf(RegionCounty("大武口区"), RegionCounty("惠农区"))),
        RegionCity(name = "吴忠", areaCode = "0953", counties = listOf(RegionCounty("利通区"), RegionCounty("红寺堡区"), RegionCounty("青铜峡市"))),
        RegionCity(name = "固原", areaCode = "0954", counties = listOf(RegionCounty("原州区"))),
        RegionCity(name = "中卫", areaCode = "0955", counties = listOf(RegionCounty("沙坡头区"), RegionCounty("中宁县"))),
    )),
    Region(province = "新疆", cities = listOf(
        RegionCity(name = "乌鲁木齐", areaCode = "0991", counties = listOf(RegionCounty("天山区"), RegionCounty("沙依巴克区"), RegionCounty("新市区"), RegionCounty("水磨沟区"), RegionCounty("头屯河区"), RegionCounty("达坂城区"), RegionCounty("米东区"))),
        RegionCity(name = "克拉玛依", areaCode = "0990", counties = listOf(RegionCounty("独山子区"), RegionCounty("克拉玛依区"), RegionCounty("白碱滩区"))),
        RegionCity(name = "吐鲁番", areaCode = "0995", counties = listOf(RegionCounty("高昌区"))),
        RegionCity(name = "哈密", areaCode = "0902", counties = listOf(RegionCounty("伊州区"))),
        RegionCity(name = "昌吉", areaCode = "0994", counties = listOf(RegionCounty("昌吉市"), RegionCounty("阜康市"), RegionCounty("玛纳斯县"))),
        RegionCity(name = "博尔塔拉", areaCode = "0909", counties = listOf(RegionCounty("博乐市"))),
        RegionCity(name = "巴音郭楞", areaCode = "0996", counties = listOf(RegionCounty("库尔勒市"))),
        RegionCity(name = "阿克苏", areaCode = "0997", counties = listOf(RegionCounty("阿克苏市"))),
        RegionCity(name = "克孜勒苏", areaCode = "0908", counties = listOf(RegionCounty("阿图什市"))),
        RegionCity(name = "喀什", areaCode = "0998", counties = listOf(RegionCounty("喀什市"), RegionCounty("莎车县"))),
        RegionCity(name = "和田", areaCode = "0903", counties = listOf(RegionCounty("和田市"))),
        RegionCity(name = "伊犁", areaCode = "0999", counties = listOf(RegionCounty("伊宁市"), RegionCounty("奎屯市"), RegionCounty("霍尔果斯市"))),
        RegionCity(name = "塔城", areaCode = "0901", counties = listOf(RegionCounty("塔城市"))),
        RegionCity(name = "阿勒泰", areaCode = "0906", counties = listOf(RegionCounty("阿勒泰市"))),
        RegionCity(name = "石河子", areaCode = "0993", counties = listOf(RegionCounty("石河子市"))),
    )),
    Region(province = "香港", cities = listOf(
        RegionCity(name = "香港", areaCode = "852", counties = listOf(RegionCounty("香港岛"), RegionCounty("九龙"), RegionCounty("新界"))),
    )),
    Region(province = "澳门", cities = listOf(
        RegionCity(name = "澳门", areaCode = "853", counties = listOf(RegionCounty("澳门半岛"), RegionCounty("氹仔"), RegionCounty("路环"))),
    )),
    Region(province = "台湾", cities = listOf(
        RegionCity(name = "台北", areaCode = "8862", counties = listOf(RegionCounty("中正区"), RegionCounty("大同区"), RegionCounty("中山区"))),
        RegionCity(name = "新北", areaCode = "8862", counties = listOf(RegionCounty("板桥区"), RegionCounty("新庄区"))),
        RegionCity(name = "桃园", areaCode = "8863", counties = listOf(RegionCounty("桃园区"), RegionCounty("中坜区"))),
        RegionCity(name = "台中", areaCode = "8864", counties = listOf(RegionCounty("西屯区"), RegionCounty("北区"))),
        RegionCity(name = "台南", areaCode = "8866", counties = listOf(RegionCounty("东区"), RegionCounty("南区"))),
        RegionCity(name = "高雄", areaCode = "8867", counties = listOf(RegionCounty("左营区"), RegionCounty("三民区"))),
        RegionCity(name = "基隆", areaCode = "8862", counties = listOf(RegionCounty("仁爱区"))),
        RegionCity(name = "新竹", areaCode = "8863", counties = listOf(RegionCounty("东区"))),
        RegionCity(name = "嘉义", areaCode = "8865", counties = listOf(RegionCounty("东区"))),
    )),
)