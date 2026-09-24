package com.example.ui.screens.toolbox

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SunsetOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

/**
 * 今天吃什么？（v1.7.4）
 * - 各大菜系子分类，可任选一个或多个菜系
 * - 随机「色子」动画，掷出菜品
 * - 可自由选择做几个菜（1-5 道）
 * - 每个菜品独一无二（同一轮不重复）
 * - 每道菜包含：配料、调味料、制作教程
 */
data class Dish(
    val name: String,
    val cuisine: String,        // 菜系
    val icon: String,
    val ingredients: String,    // 配料
    val seasonings: String,     // 调味料
    val steps: List<String>     // 制作教程
)

private val DISH_DATABASE = listOf(
    // ============ 川菜 ============
    Dish("麻婆豆腐", "川菜", "🌶️", "嫩豆腐、牛肉末、蒜苗、姜蒜末", "豆瓣酱、花椒粉、辣椒面、生抽、盐、糖、淀粉", listOf("豆腐切块焯水去豆腥", "热油炒香牛肉末，下豆瓣酱炒出红油", "加姜蒜末、辣椒面炒香，加水烧开", "下豆腐轻推煮 3 分钟，勾芡收汁", "撒花椒粉与蒜苗出锅")),
    Dish("宫保鸡丁", "川菜", "🥜", "鸡胸肉、花生米、干辣椒、葱段、黄瓜", "生抽、醋、糖、料酒、淀粉、花椒", listOf("鸡丁用料酒淀粉腌制 15 分钟", "调碗汁：生抽+醋+糖+淀粉+水", "热油滑熟鸡丁盛出", "爆香干辣椒花椒，倒入鸡丁与料汁", "加花生米葱段大火翻炒出锅")),
    Dish("水煮鱼", "川菜", "🐟", "草鱼片、豆芽、千张、干辣椒、花椒、姜蒜", "豆瓣酱、料酒、淀粉、盐、辣椒面、蛋清", listOf("鱼片加蛋清淀粉盐腌制", "豆芽千张焯水铺碗底", "豆瓣酱炒出红油加水烧开下鱼片", "煮至变色连汤倒入碗中", "铺辣椒面干辣椒花椒，热油淋上激香")),
    Dish("回锅肉", "川菜", "🥓", "五花肉、蒜苗、青椒、姜片", "豆瓣酱、甜面酱、生抽、白糖、豆豉", listOf("五花肉冷水下锅煮 20 分钟切薄片", "煸炒至出油卷起呈灯盏窝", "下豆瓣酱豆豉炒出红油", "加甜面酱生抽白糖调味", "下蒜苗青椒大火快炒出锅")),
    Dish("夫妻肺片", "川菜", "🥩", "牛头皮、牛心、牛舌、花生碎、香菜", "红油、花椒油、生抽、醋、白糖、芝麻", listOf("牛杂卤熟切片摆盘", "调红油料汁（红油+花椒油+生抽+醋+糖）", "料汁浇在牛杂上", "撒花生碎、白芝麻、香菜即可")),
    // ============ 粤菜 ============
    Dish("白切鸡", "粤菜", "🍗", "三黄鸡、姜、葱", "盐、花生油、酱油、蒜蓉", listOf("整鸡开水下锅浸煮 25 分钟", "捞出立即入冰水收紧鸡皮", "斩件摆盘", "姜蓉蒜蓉+盐+热油调蘸料，淋酱油即可")),
    Dish("清蒸鲈鱼", "粤菜", "🐠", "鲈鱼、姜丝、葱丝、红椒丝", "蒸鱼豉油、料酒、花生油、盐", listOf("鲈鱼两面打花刀，料酒盐抹匀", "铺姜丝水开大火蒸 8 分钟", "倒掉汤汁，铺葱丝红椒丝", "淋热油激香，浇蒸鱼豉油即可")),
    Dish("虾仁炒蛋", "粤菜", "🍤", "鲜虾仁、鸡蛋、葱花", "盐、料酒、白胡椒粉、牛奶", listOf("虾仁料酒盐腌 10 分钟", "鸡蛋加少许牛奶盐打散", "热油滑炒虾仁至变色盛出", "倒入蛋液快炒，半凝固时回虾仁", "撒葱花炒匀出锅")),
    Dish("蜜汁叉烧", "粤菜", "🍖", "梅花肉、姜蒜", "叉烧酱、蜂蜜、生抽、料酒", listOf("梅花肉用叉烧酱生抽料酒腌一夜", "烤箱 200 度烤 20 分钟", "刷蜂蜜翻面再烤 15 分钟", "晾凉切片装盘即可")),
    // ============ 湘菜 ============
    Dish("剁椒鱼头", "湘菜", "🐡", "胖头鱼鱼头、剁椒、姜末、蒜末、葱花", "蒸鱼豉油、料酒、白糖、猪油", listOf("鱼头对半剖开，料酒盐抹匀腌 10 分钟", "铺满剁椒与姜蒜末", "水开大火蒸 12 分钟", "淋蒸鱼豉油，铺葱花浇热油即可")),
    Dish("小炒黄牛肉", "湘菜", "🐮", "黄牛肉、小米辣、泡椒、蒜、香菜", "生抽、蚝油、淀粉、料酒、油", listOf("牛肉逆纹切薄片，料酒淀粉腌 10 分钟", "热锅冷油快速滑炒牛肉至变色盛出", "爆香蒜末小米辣泡椒", "回牛肉加生抽蚝油大火爆炒", "撒香菜段炒匀出锅")),
    Dish("辣椒炒肉", "湘菜", "🌶️", "五花肉、螺丝椒、蒜片", "生抽、老抽、豆豉、盐", listOf("螺丝椒拍扁切段干煸至虎皮盛出", "五花肉煸炒出油", "下蒜片豆豉炒香", "回辣椒加生抽老抽盐，大火翻炒出锅")),
    // ============ 江浙菜 ============
    Dish("红烧肉", "江浙菜", "🍖", "五花肉、姜片、葱结、八角", "冰糖、生抽、老抽、料酒", listOf("五花肉切块焯水", "小火炒糖色至琥珀色", "下肉块翻炒上色", "加生抽老抽料酒热水，小火炖 40 分钟", "大火收汁至浓稠即可")),
    Dish("西湖醋鱼", "江浙菜", "🐟", "草鱼、姜末", "香醋、白糖、生抽、料酒、淀粉", listOf("草鱼剖两半汆水 3 分钟", "用糖醋汁（醋+糖+生抽）烧开", "湿淀粉勾芡成琉璃芡", "浇在鱼身上撒姜末即可")),
    Dish("龙井虾仁", "江浙菜", "🍤", "鲜虾仁、龙井茶叶、蛋清", "盐、料酒、淀粉、猪油", listOf("虾仁用蛋清淀粉盐上浆", "龙井茶泡开沥出茶叶", "温油滑炒虾仁至变白", "下茶叶与少许茶汁翻炒即可")),
    // ============ 东北菜 ============
    Dish("锅包肉", "东北菜", "🥢", "猪里脊、淀粉、葱姜丝、胡萝卜丝", "白醋、白糖、生抽、盐", listOf("里脊切大片，淀粉糊挂浆", "油温六成热炸至定型捞出", "复炸至金黄酥脆", "调糖醋汁烧开，下肉片快速裹汁", "撒葱姜丝胡萝卜丝出锅")),
    Dish("猪肉炖粉条", "东北菜", "🍲", "五花肉、红薯粉条、白菜、姜葱", "生抽、老抽、盐、八角、桂皮", listOf("五花肉煸炒出油", "加葱姜八角桂皮炒香，烹生抽老抽", "加热水炖 20 分钟", "下粉条白菜再炖 15 分钟", "收汁调味即可")),
    Dish("地三鲜", "东北菜", "🍆", "茄子、土豆、青椒、蒜末", "生抽、老抽、蚝油、糖、淀粉", listOf("土豆茄子切块炸至金黄", "青椒过油断生", "调碗汁（生抽+蚝油+糖+淀粉）", "蒜末爆香，回所有食材淋汁", "大火翻炒收汁即可")),
    // ============ 家常菜 ============
    Dish("番茄炒蛋", "家常菜", "🍅", "番茄、鸡蛋、葱花", "盐、白糖、番茄酱", listOf("鸡蛋加盐打散炒熟盛出", "番茄切块炒出汁", "加少许白糖番茄酱调味", "回鸡蛋翻炒，撒葱花出锅")),
    Dish("可乐鸡翅", "家常菜", "🍗", "鸡翅中、姜片、可乐", "生抽、老抽、料酒、盐", listOf("鸡翅两面划刀焯水", "煎至两面金黄", "倒入可乐没过鸡翅，加生抽老抽", "中小火焖 15 分钟", "大火收汁至浓稠即可")),
    Dish("鱼香肉丝", "家常菜", "🥕", "猪里脊、木耳、胡萝卜、青椒、笋丝", "豆瓣酱、糖、醋、生抽、淀粉", listOf("肉丝料酒淀粉腌 10 分钟", "热油滑熟肉丝盛出", "豆瓣酱炒出红油", "下配菜翻炒，回肉丝淋鱼香汁", "大火翻炒收汁出锅")),
    Dish("酸辣土豆丝", "家常菜", "🥔", "土豆、干辣椒、青红椒丝、蒜片", "白醋、盐、花椒", listOf("土豆切细丝泡水去淀粉", "热油爆香花椒干辣椒蒜片", "大火快炒土豆丝", "沿锅边烹醋，加盐调味", "加青红椒丝炒匀出锅")),
    Dish("糖醋排骨", "家常菜", "🍖", "肋排、姜片、白芝麻", "冰糖、香醋、生抽、料酒", listOf("排骨焯水后煎至微黄", "加冰糖炒出糖色", "烹料酒生抽，加热水炖 30 分钟", "加香醋大火收汁", "撒白芝麻装盘")),
    // ============ 素食 ============
    Dish("手撕包菜", "素食", "🥬", "包菜、干辣椒、蒜片", "生抽、陈醋、盐、白糖", listOf("包菜手撕成片洗净沥干", "热油爆香干辣椒蒜片", "大火快炒包菜至断生", "淋生抽陈醋，加盐糖调味出锅")),
    Dish("地三鲜(素)", "素食", "🍆", "茄子、土豆、青椒、蒜末", "生抽、蚝油、糖、淀粉", listOf("茄子土豆切块蒸熟（少油版）", "青椒煸炒", "调碗汁淋入", "全部回锅大火收汁即可")),
    Dish("蒜蓉西兰花", "素食", "🥦", "西兰花、蒜末、枸杞", "盐、蚝油、淀粉、花生油", listOf("西兰花掰小朵焯水 1 分钟", "热油爆香蒜末", "下西兰花翻炒", "加蚝油盐调味，勾薄芡出锅")),
    // ============ 汤羹 ============
    Dish("西红柿鸡蛋汤", "汤羹", "🍲", "番茄、鸡蛋、葱花", "盐、白胡椒粉、香油", listOf("番茄炒出沙", "加水烧开", "淋入打散的蛋液成蛋花", "加盐白胡椒粉，点香油撒葱花即可")),
    Dish("玉米排骨汤", "汤羹", "🌽", "排骨、甜玉米、胡萝卜、姜片", "盐、料酒", listOf("排骨焯水去沫", "与玉米胡萝卜姜片同入锅", "加足量水大火烧开转小火", "炖 60 分钟，加盐调味即可")),
    // ============ 主食 ============
    Dish("蛋炒饭", "主食", "🍚", "隔夜米饭、鸡蛋、火腿丁、葱花", "盐、生抽、白胡椒粉", listOf("鸡蛋炒散盛出", "米饭下锅炒散炒透", "回鸡蛋，加火腿丁翻炒", "淋生抽撒盐，大火炒香", "撒葱花出锅")),
    Dish("番茄鸡蛋面", "主食", "🍜", "面条、番茄、鸡蛋、青菜", "盐、生抽、葱花、香油", listOf("番茄炒出沙加水烧开", "下面条煮 3 分钟", "淋入蛋液成蛋花", "下青菜，加盐生抽调味", "点香油撒葱花出锅")),
    // ============ 甜品小吃 ============
    Dish("拔丝地瓜", "甜品小吃", "🍠", "红薯、白糖", "食用油、白芝麻", listOf("红薯切滚刀块炸至金黄", "锅中糖加水小火熬至琥珀色", "下红薯快速翻裹糖浆", "装盘拉丝，撒白芝麻，配凉水碗")),
    Dish("红糖糍粑", "甜品小吃", "🍡", "糯米粉、温水、黄豆粉", "红糖、白糖", listOf("糯米粉加温水揉团", "整形成长条煎至金黄", "红糖加水熬成糖浆", "浇在糍粑上撒黄豆粉即可")),
    // ============ 西餐 ============
    Dish("黑椒牛柳意面", "西餐", "🍝", "意大利面、牛里脊、彩椒、洋葱", "黑胡椒酱、生抽、盐、橄榄油", listOf("意面煮 9 分钟过凉水", "牛柳条黑椒酱腌制", "热油煎牛柳至七分熟", "下洋葱彩椒翻炒，回意面与黑椒酱", "翻拌均匀出锅")),
    Dish("奶油蘑菇汤", "西餐", "🥣", "口蘑、洋葱、淡奶油、黄油", "盐、黑胡椒、面粉", listOf("黄油炒香洋葱末", "下蘑菇片炒软撒面粉炒匀", "加水煮 10 分钟", "料理机打细腻回锅", "加淡奶油盐黑胡椒调味即可")),
    // ============ 日韩料理 ============
    Dish("寿喜烧", "日韩料理", "🍲", "肥牛、豆腐、香菇、娃娃菜、茼蒿、魔芋丝", "寿喜烧汁（酱油+味淋+糖+清酒）、黄油", listOf("黄油融化煎香肥牛卷", "倒入寿喜烧汁", "下豆腐香菇娃娃菜等焖煮", "边煮边吃，蘸生蛋液更嫩滑")),
    Dish("韩式辣炒年糕", "日韩料理", "🍥", "年糕条、鱼饼、洋葱、大葱", "韩式辣酱、糖、生抽、芝麻", listOf("年糕煮软", "韩式辣酱+糖+生抽+水调酱汁", "酱汁煮开下年糕鱼饼洋葱", "煮至浓稠撒芝麻葱花即可")),
    Dish("日式咖喱鸡饭", "日韩料理", "🍛", "鸡腿肉、土豆、胡萝卜、洋葱、米饭", "咖喱块、生抽、料酒", listOf("鸡肉煎至金黄", "下洋葱炒软，加土豆胡萝卜翻炒", "加水没过食材煮 15 分钟", "关火加咖喱块融化", "小火煮至浓稠浇在米饭上")),
    // ============ 烧烤 ============
    Dish("蜜汁烤鸡翅", "烧烤", "🍗", "鸡翅中、姜蒜", "蜂蜜、生抽、蚝油、黑胡椒、孜然", listOf("鸡翅划刀，生抽蚝油蜂蜜腌 2 小时", "烤箱 200 度烤 15 分钟", "翻面刷腌汁再烤 10 分钟", "撒孜然黑胡椒再烤 2 分钟即可")),
    Dish("孜然羊肉串", "烧烤", "🍢", "羊腿肉、洋葱", "孜然粉、辣椒面、盐、生抽、蛋清", listOf("羊肉切块洋葱生抽腌 1 小时", "穿串，烤 5 分钟", "刷油撒孜然辣椒面", "翻面再烤 5 分钟至滋滋冒油")),
    // ============ 火锅 ============
    Dish("番茄牛腩锅", "火锅", "🍅", "牛腩、番茄、土豆、香菇、金针菇、各类涮菜", "番茄酱、生抽、盐、姜片、八角", listOf("牛腩焯水炖 40 分钟", "番茄炒出沙加番茄酱", "倒入牛腩汤中煮开", "下土豆香菇等配菜", "涮菜上桌开吃")),
    Dish("菌菇鸡汤锅", "火锅", "🍄", "半只鸡、各类菌菇、豆腐、娃娃菜、虾滑", "盐、姜片、枸杞、料酒", listOf("鸡肉焯水后加姜片炖 30 分钟", "下菌菇继续炖 15 分钟", "加盐枸杞调味", "涮豆腐娃娃菜虾滑等配菜")),
    // ============ 面点 ============
    Dish("韭菜鸡蛋饺子", "面点", "🥟", "饺子皮、韭菜、鸡蛋、虾皮", "盐、香油、生抽、白胡椒粉", listOf("鸡蛋炒碎放凉", "韭菜切末与鸡蛋虾皮拌匀", "加盐香油白胡椒粉调味", "包饺子，水开下锅煮至浮起再煮 2 分钟")),
    Dish("葱油饼", "面点", "🫓", "面粉、小葱、温水", "盐、油、五香粉", listOf("面粉温水揉团醒 30 分钟", "擀开撒盐五香粉葱花，卷起盘圆", "再擀成饼，平底锅烙至两面金黄", "层层酥脆，撕开即食")),
)

private val CUISINES = listOf("川菜", "粤菜", "湘菜", "江浙菜", "东北菜", "家常菜", "素食", "汤羹", "主食", "甜品小吃", "西餐", "日韩料理", "烧烤", "火锅", "面点")

@androidx.compose.foundation.layout.ExperimentalLayoutApi
@Composable
fun FoodPickerScreenView(modifier: Modifier = Modifier) {
    var selectedCuisines by remember { mutableStateOf(setOf<String>()) }
    var dishCount by remember { mutableIntStateOf(1) }
    var result by remember { mutableStateOf<List<Dish>>(emptyList()) }
    var rolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 色子动画：旋转角度
    var diceRotation by remember { mutableStateOf(0f) }
    val spin by androidx.compose.animation.core.animateFloatAsState(
        targetValue = diceRotation,
        animationSpec = tween(900),
        label = "diceSpin"
    )

    /** 随机色子：从已选菜系中抽取 N 道不重复菜品 */
    fun rollDice() {
        val pool = if (selectedCuisines.isEmpty()) DISH_DATABASE else DISH_DATABASE.filter { it.cuisine in selectedCuisines }
        if (pool.isEmpty()) return
        val count = dishCount.coerceIn(1, pool.size)
        val shuffled = pool.shuffled().take(count)
        result = shuffled
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顶部标题卡
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(SunsetOrange, FlameRed))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍽️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "今天吃什么？· 随机美食骰",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "摇一摇随机出菜 · 每道菜含配料/调味料/制作教程 · 每菜系独一无二",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 菜系选择（子分类，可多选）
        item {
            Text(
                text = "选择菜系（不选 = 全部菜系）",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            // v1.7.6 修复：移除嵌套 LazyColumn（外层已是 LazyColumn，内嵌滚动容器会崩溃），改为 FlowRow 平铺
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CUISINES.forEach { cuisine ->
                    val selected = cuisine in selectedCuisines
                    Surface(
                        onClick = {
                            selectedCuisines = if (selected) selectedCuisines - cuisine else selectedCuisines + cuisine
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) SunsetOrange.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (selected) SunsetOrange else Color.Transparent)
                    ) {
                        Text(
                            text = cuisine,
                            fontSize = 11.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) SunsetOrange else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 做几个菜
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "做几个菜：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                listOf(1, 2, 3, 4, 5).forEach { n ->
                    val selected = dishCount == n
                    Surface(
                        onClick = { dishCount = n },
                        shape = CircleShape,
                        color = if (selected) FlameRed else Color.White.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (selected) FlameRed else Color.Transparent),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "$n",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 随机色子按钮
        item {
            Button(
                onClick = {
                    scope.launch {
                        rolling = true
                        result = emptyList()
                        // 色子转动 6 次（每次随机停一下）
                        repeat(6) {
                            diceRotation += 360f
                            delay(120)
                        }
                        diceRotation += 360f
                        delay(180)
                        rollDice()
                        rolling = false
                    }
                },
                enabled = !rolling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .graphicsLayer { rotationZ = spin },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎲", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (rolling) "摇骰子中…" else "🎲 随机色子 · 摇出${dishCount}道菜",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // 结果菜品卡片
        if (result.isNotEmpty()) {
            item {
                Text(
                    text = "🍳 今日菜单（${result.size} 道 · 每个菜系独一无二）",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(result) { dish ->
                DishCard(dish)
            }
        } else if (!rolling) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍚", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "点击上方「随机色子」，摇出今天吃什么！",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DishCard(dish: Dish) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.68f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dish.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dish.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = dish.cuisine,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            InfoRow("🧂 配料", dish.ingredients, Color(0xFFE65100))
            Spacer(modifier = Modifier.height(6.dp))
            InfoRow("🌶️ 调味料", dish.seasonings, Color(0xFFC2185B))

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "👨‍🍳 制作教程",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            dish.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(JadeGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = JadeGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
    }
}
