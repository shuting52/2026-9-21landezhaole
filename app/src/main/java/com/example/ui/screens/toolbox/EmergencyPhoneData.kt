package com.example.ui.screens.toolbox

/**
 * 紧急电话工具 · 数据（v1.0.4）
 *
 * 覆盖全国：全国通用权威号码（急救/报警/政务/社保/银行/快递/电商/外卖/投诉/交通/保险等分类）
 * + 支持地区选择到 省 → 市 → 区/县（内置 34 个省级行政区与重点地级市/区县，含区号）。
 * 每个号码均可一键快捷呼出（打开拨号盘预填并直接呼出）。
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

/** 省级行政区数据（地级市/区县 + 区号） */
data class RegionCity(
    val name: String,
    val areaCode: String
)

data class Region(
    val province: String,
    val cities: List<RegionCity>
)

/** 全国通用分类电话（号码均为公开权威热线） */
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
            EmergencyNumber("劳动争议仲裁", "12333", "劳动争议调解仲裁咨询"),
            EmergencyNumber("法律援助", "12348", "劳动纠纷免费法律咨询"),
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
            EmergencyNumber("供电服务", "95598", "国家电网统一服务热线"),
            EmergencyNumber("南方电网", "95598", "南方电网服务热线"),
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

/** 全国省级行政区与地级市/区县数据（区号为常用区号） */
val NATIONAL_REGIONS: List<Region> = listOf(
    Region("北京", listOf(
        RegionCity("北京市区", "010"),
        RegionCity("昌平区", "010"),
        RegionCity("通州区", "010"),
        RegionCity("顺义区", "010"),
        RegionCity("大兴区", "010")
    )),
    Region("天津", listOf(
        RegionCity("天津市", "022"),
        RegionCity("滨海新区", "022"),
        RegionCity("武清区", "022"),
        RegionCity("宝坻区", "022")
    )),
    Region("河北", listOf(
        RegionCity("石家庄", "0311"),
        RegionCity("唐山", "0315"),
        RegionCity("保定", "0312"),
        RegionCity("邯郸", "0310"),
        RegionCity("廊坊", "0316"),
        RegionCity("秦皇岛", "0335")
    )),
    Region("山西", listOf(
        RegionCity("太原", "0351"),
        RegionCity("大同", "0352"),
        RegionCity("临汾", "0357"),
        RegionCity("运城", "0359"),
        RegionCity("长治", "0355")
    )),
    Region("内蒙古", listOf(
        RegionCity("呼和浩特", "0471"),
        RegionCity("包头", "0472"),
        RegionCity("鄂尔多斯", "0477"),
        RegionCity("赤峰", "0476"),
        RegionCity("通辽", "0475")
    )),
    Region("辽宁", listOf(
        RegionCity("沈阳", "024"),
        RegionCity("大连", "0411"),
        RegionCity("鞍山", "0412"),
        RegionCity("抚顺", "0413"),
        RegionCity("锦州", "0416")
    )),
    Region("吉林", listOf(
        RegionCity("长春", "0431"),
        RegionCity("吉林", "0432"),
        RegionCity("四平", "0434"),
        RegionCity("延边", "0433")
    )),
    Region("黑龙江", listOf(
        RegionCity("哈尔滨", "0451"),
        RegionCity("齐齐哈尔", "0452"),
        RegionCity("大庆", "0459"),
        RegionCity("牡丹江", "0453")
    )),
    Region("上海", listOf(
        RegionCity("上海市", "021"),
        RegionCity("浦东新区", "021"),
        RegionCity("闵行区", "021"),
        RegionCity("嘉定区", "021"),
        RegionCity("松江区", "021")
    )),
    Region("江苏", listOf(
        RegionCity("南京", "025"),
        RegionCity("苏州", "0512"),
        RegionCity("无锡", "0510"),
        RegionCity("常州", "0519"),
        RegionCity("南通", "0513"),
        RegionCity("徐州", "0516"),
        RegionCity("扬州", "0514")
    )),
    Region("浙江", listOf(
        RegionCity("杭州", "0571"),
        RegionCity("宁波", "0574"),
        RegionCity("温州", "0577"),
        RegionCity("绍兴", "0575"),
        RegionCity("嘉兴", "0573"),
        RegionCity("金华", "0579"),
        RegionCity("台州", "0576")
    )),
    Region("安徽", listOf(
        RegionCity("合肥", "0551"),
        RegionCity("芜湖", "0553"),
        RegionCity("蚌埠", "0552"),
        RegionCity("安庆", "0556"),
        RegionCity("阜阳", "0558")
    )),
    Region("福建", listOf(
        RegionCity("福州", "0591"),
        RegionCity("厦门", "0592"),
        RegionCity("泉州", "0595"),
        RegionCity("漳州", "0596"),
        RegionCity("莆田", "0594")
    )),
    Region("江西", listOf(
        RegionCity("南昌", "0791"),
        RegionCity("赣州", "0797"),
        RegionCity("九江", "0792"),
        RegionCity("上饶", "0793"),
        RegionCity("宜春", "0795")
    )),
    Region("山东", listOf(
        RegionCity("济南", "0531"),
        RegionCity("青岛", "0532"),
        RegionCity("烟台", "0535"),
        RegionCity("潍坊", "0536"),
        RegionCity("临沂", "0539"),
        RegionCity("淄博", "0533")
    )),
    Region("河南", listOf(
        RegionCity("郑州", "0371"),
        RegionCity("洛阳", "0379"),
        RegionCity("开封", "0371"),
        RegionCity("南阳", "0377"),
        RegionCity("新乡", "0373"),
        RegionCity("信阳", "0376")
    )),
    Region("湖北", listOf(
        RegionCity("武汉", "027"),
        RegionCity("宜昌", "0717"),
        RegionCity("襄阳", "0710"),
        RegionCity("荆州", "0716"),
        RegionCity("黄冈", "0713")
    )),
    Region("湖南", listOf(
        RegionCity("长沙", "0731"),
        RegionCity("株洲", "0731"),
        RegionCity("湘潭", "0731"),
        RegionCity("岳阳", "0730"),
        RegionCity("衡阳", "0734"),
        RegionCity("常德", "0736")
    )),
    Region("广东", listOf(
        RegionCity("广州", "020"),
        RegionCity("深圳", "0755"),
        RegionCity("佛山", "0757"),
        RegionCity("东莞", "0769"),
        RegionCity("珠海", "0756"),
        RegionCity("惠州", "0752"),
        RegionCity("中山", "0760")
    )),
    Region("广西", listOf(
        RegionCity("南宁", "0771"),
        RegionCity("柳州", "0772"),
        RegionCity("桂林", "0773"),
        RegionCity("梧州", "0774"),
        RegionCity("北海", "0779")
    )),
    Region("海南", listOf(
        RegionCity("海口", "0898"),
        RegionCity("三亚", "0898"),
        RegionCity("儋州", "0898"),
        RegionCity("琼海", "0898")
    )),
    Region("重庆", listOf(
        RegionCity("重庆市", "023"),
        RegionCity("万州区", "023"),
        RegionCity("涪陵区", "023"),
        RegionCity("永川区", "023"),
        RegionCity("江津区", "023")
    )),
    Region("四川", listOf(
        RegionCity("成都", "028"),
        RegionCity("绵阳", "0816"),
        RegionCity("德阳", "0838"),
        RegionCity("宜宾", "0831"),
        RegionCity("南充", "0817"),
        RegionCity("泸州", "0830")
    )),
    Region("贵州", listOf(
        RegionCity("贵阳", "0851"),
        RegionCity("遵义", "0851"),
        RegionCity("六盘水", "0858"),
        RegionCity("毕节", "0857")
    )),
    Region("云南", listOf(
        RegionCity("昆明", "0871"),
        RegionCity("大理", "0872"),
        RegionCity("丽江", "0888"),
        RegionCity("曲靖", "0874"),
        RegionCity("玉溪", "0877")
    )),
    Region("西藏", listOf(
        RegionCity("拉萨", "0891"),
        RegionCity("日喀则", "0892"),
        RegionCity("林芝", "0894")
    )),
    Region("陕西", listOf(
        RegionCity("西安", "029"),
        RegionCity("咸阳", "029"),
        RegionCity("宝鸡", "0917"),
        RegionCity("延安", "0911"),
        RegionCity("渭南", "0913")
    )),
    Region("甘肃", listOf(
        RegionCity("兰州", "0931"),
        RegionCity("天水", "0938"),
        RegionCity("酒泉", "0937"),
        RegionCity("敦煌", "0937")
    )),
    Region("青海", listOf(
        RegionCity("西宁", "0971"),
        RegionCity("海东", "0972"),
        RegionCity("格尔木", "0979")
    )),
    Region("宁夏", listOf(
        RegionCity("银川", "0951"),
        RegionCity("石嘴山", "0952"),
        RegionCity("吴忠", "0953")
    )),
    Region("新疆", listOf(
        RegionCity("乌鲁木齐", "0991"),
        RegionCity("克拉玛依", "0990"),
        RegionCity("喀什", "0998"),
        RegionCity("伊犁", "0999")
    )),
    Region("香港", listOf(
        RegionCity("香港", "852")
    )),
    Region("澳门", listOf(
        RegionCity("澳门", "853")
    )),
    Region("台湾", listOf(
        RegionCity("台北", "8862"),
        RegionCity("高雄", "8867"),
        RegionCity("台中", "8864")
    ))
)