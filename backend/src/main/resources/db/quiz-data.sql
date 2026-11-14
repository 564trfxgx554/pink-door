-- 默契问答题库：100 道题（原有 20 道 + 新增 80 道）。
-- 用法：替换 netbar-backend/src/main/resources/db/quiz-data.sql，重启后端即可。
-- 按题目内容判重，每次启动执行也不会重复插入。
-- options 存 JSON 数组，Java 用 Jackson 解析。
-- 分类只写在注释里，表结构没加分类字段，不用改表。

-- ================= 原有 20 道 =================

INSERT INTO pd_quiz_question (content, options)
SELECT '约会最想去哪？', '["海边","山里","游乐场","宅家看电影"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '约会最想去哪？');

INSERT INTO pd_quiz_question (content, options)
SELECT '周末最想吃什么？', '["火锅","烧烤","日料","奶茶+小吃"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '周末最想吃什么？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的季节？', '["春天","夏天","秋天","冬天"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的季节？');

INSERT INTO pd_quiz_question (content, options)
SELECT '更喜欢哪种电影？', '["喜剧","爱情","悬疑","动画"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '更喜欢哪种电影？');

INSERT INTO pd_quiz_question (content, options)
SELECT '闲下来最想干嘛？', '["打游戏","刷剧","出去逛","睡觉"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '闲下来最想干嘛？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的颜色？', '["粉色","蓝色","白色","黑色"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的颜色？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最爱吃的水果？', '["草莓","芒果","西瓜","葡萄"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最爱吃的水果？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方生气时最想要什么？', '["抱抱","哄哄","自己静静","买好吃的"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方生气时最想要什么？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的 Sanrio 角色？', '["Hello Kitty","美乐蒂","玉桂狗","库洛米"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的 Sanrio 角色？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方手机里最多的照片是？', '["自拍","美食","风景","你/对方"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方手机里最多的照片是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最想去的城市？', '["上海","成都","大理","三亚"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最想去的城市？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最怕什么？', '["黑暗","虫子","高处","打针"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最怕什么？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果只能带一样东西去荒岛，你会带？', '["手机","零食","帐篷","对方"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果只能带一样东西去荒岛，你会带？');

INSERT INTO pd_quiz_question (content, options)
SELECT '下雨天最想干嘛？', '["宅家看剧","出去踩水","睡觉","喝奶茶"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '下雨天最想干嘛？');

INSERT INTO pd_quiz_question (content, options)
SELECT '吵架后谁先低头？', '["我","TA","看谁错了","一起冷静"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '吵架后谁先低头？');

INSERT INTO pd_quiz_question (content, options)
SELECT '理想中的约会是？', '["看日落","吃大餐","逛街","在家做饭"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '理想中的约会是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最常说的一句话是？', '["好饿","好困","好喜欢你","随便"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最常说的一句话是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最想一起做的事？', '["旅行","养只猫","开家小店","一起变老"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最想一起做的事？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果中了 500 万，第一件事是？', '["买房","旅行","存起来","给对方买礼物"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果中了 500 万，第一件事是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次约会是在哪？', '["电影院","餐厅","公园","奶茶店"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次约会是在哪？');

-- ================= 新增 80 道：轻松类（21-40） =================

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的奶茶口味？', '["珍珠奶茶","水果茶","抹茶拿铁","不喝奶茶"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的奶茶口味？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的零食？', '["薯片","巧克力","辣条","坚果"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的零食？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的饮料？', '["可乐","奶茶","果汁","白开水"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的饮料？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的早餐？', '["包子豆浆","面包牛奶","粥+咸菜","不吃早餐"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的早餐？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最想养的宠物？', '["猫","狗","兔子","仓鼠"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最想养的宠物？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的运动？', '["跑步","游泳","羽毛球","散步"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的运动？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的歌是哪种？', '["流行","古风","摇滚","轻音乐"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的歌是哪种？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的游戏类型？', '["消消乐","王者类","恐怖解谜","种田养成"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的游戏类型？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的天气？', '["晴天","阴天","雨天","雪天"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的天气？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的一天中的时间？', '["清晨","午后","傍晚","深夜"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的一天中的时间？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最想去的游乐项目？', '["过山车","摩天轮","旋转木马","鬼屋"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最想去的游乐项目？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的甜品？', '["蛋糕","冰淇淋","布丁","糖水"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的甜品？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的火锅蘸料？', '["麻酱","香油蒜泥","干碟","沙茶酱"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的火锅蘸料？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的夜宵？', '["烧烤","小龙虾","泡面","关东煮"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的夜宵？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的出行方式？', '["走路","骑车","开车","坐地铁"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的出行方式？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的拍照姿势？', '["比耶","比心","捂脸","大笑"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的拍照姿势？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的睡姿？', '["平躺","侧卧","趴着","抱着东西睡"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的睡姿？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的解压方式？', '["吃好吃的","打游戏","睡觉","找对方聊天"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的解压方式？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的节日？', '["春节","情人节","圣诞节","生日"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的节日？');

INSERT INTO pd_quiz_question (content, options)
SELECT '最喜欢的花？', '["玫瑰","樱花","向日葵","薰衣草"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '最喜欢的花？');

-- ================= 新增 80 道：了解类（41-60） =================

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的菜？', '["红烧肉","番茄炒蛋","麻辣烫","白灼虾"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的菜？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最不喜欢吃的东西？', '["香菜","榴莲","肥肉","苦瓜"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最不喜欢吃的东西？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的动漫？', '["宫崎骏","新海诚","柯南","海贼王"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的动漫？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的歌手？', '["周杰伦","林俊杰","陈奕迅","薛之谦"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的歌手？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的书？', '["小说","漫画","散文","不看书"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的书？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的穿搭风格？', '["休闲","甜美","酷酷的","简约"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的穿搭风格？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方起床气大不大？', '["很大","有一点","没有","越睡越精神"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方起床气大不大？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最容易为什么事笑？', '["冷笑话","表情包","对方出糗","电视剧"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最容易为什么事笑？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最在意什么？', '["外貌","成绩","对方的感受","钱"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最在意什么？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最擅长的事？', '["做饭","打游戏","学习","哄人"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最擅长的事？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最不擅长的事？', '["做饭","运动","认路","早起"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最不擅长的事？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的聊天方式？', '["发语音","打字","发视频","打电话"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的聊天方式？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方手机里最常用的 App 是？', '["微信","短视频","音乐","游戏"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方手机里最常用的 App 是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的天气穿搭？', '["裙子","卫衣","牛仔裤","毛衣"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的天气穿搭？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的约会时间？', '["上午","下午","傍晚","晚上"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的约会时间？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最害怕的动物？', '["蜘蛛","蛇","蟑螂","老鼠"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最害怕的动物？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的冰淇淋口味？', '["香草","巧克力","草莓","抹茶"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的冰淇淋口味？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的 Sanrio 角色第二名？', '["帕恰狗","大耳狗","凯蒂猫","酷洛米"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的 Sanrio 角色第二名？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最喜欢的科目？', '["数学","英语","语文","计算机"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最喜欢的科目？');

INSERT INTO pd_quiz_question (content, options)
SELECT '对方最讨厌的科目？', '["数学","英语","物理","化学"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '对方最讨厌的科目？');

-- ================= 新增 80 道：默契类（61-80） =================

INSERT INTO pd_quiz_question (content, options)
SELECT '如果明天放假，最想去哪？', '["海边","山里","游乐场","宅家"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果明天放假，最想去哪？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以穿越，最想去哪个朝代？', '["唐朝","宋朝","明朝","未来"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以穿越，最想去哪个朝代？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以拥有一种超能力，想要？', '["隐身","读心术","时间暂停","瞬间移动"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以拥有一种超能力，想要？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以变成一种动物，想变成？', '["猫","狗","鸟","鱼"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以变成一种动物，想变成？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果只能选一种，你会选？', '["有钱","有颜","有才华","有对方"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果只能选一种，你会选？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果给对方起个外号，会叫？', '["小可爱","小笨蛋","宝贝","小公主"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果给对方起个外号，会叫？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果对方生气了，你会？', '["马上哄","先冷静一下","买好吃的","讲笑话"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果对方生气了，你会？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果下雨没带伞，你会？', '["淋雨跑回家","躲雨等停","买把伞","叫对方来接"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果下雨没带伞，你会？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果考试前一晚，你会？', '["通宵复习","早点睡","临时抱佛脚","放弃治疗"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果考试前一晚，你会？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以和对方交换一天，你会？', '["替对方上课","替对方吃饭","替对方睡觉","替对方打游戏"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以和对方交换一天，你会？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果你们开一家店，想开？', '["奶茶店","花店","书店","小吃店"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果你们开一家店，想开？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以去太空，最想看？', '["地球","月亮","星星","黑洞"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以去太空，最想看？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果回到小时候，最想做？', '["多玩一会","好好学习","多陪家人","再认识对方一次"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果回到小时候，最想做？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果只能保留一张照片，会留？', '["合照","对方的单人照","风景照","毕业照"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果只能保留一张照片，会留？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果对方送你礼物，最想要？', '["手写信","花","吃的","什么都好"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果对方送你礼物，最想要？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果你们吵架了，最希望对方？', '["先道歉","抱抱我","给我买好吃的","让我静静"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果你们吵架了，最希望对方？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以许一个愿望，你会许？', '["永远在一起","身体健康","发大财","世界和平"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以许一个愿望，你会许？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果对方是你的宠物，你希望 TA 是？', '["猫","狗","兔子","小熊"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果对方是你的宠物，你希望 TA 是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果可以选一个地方定居，想去？', '["大城市","小县城","海边","山里"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果可以选一个地方定居，想去？');

INSERT INTO pd_quiz_question (content, options)
SELECT '如果对方明天要出远门，你会？', '["送到车站","在家等","一起去","舍不得"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '如果对方明天要出远门，你会？');

-- ================= 新增 80 道：回忆 / 日常类（81-100） =================

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次聊天是在哪？', '["微信","QQ","面对面","游戏里"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次聊天是在哪？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次见面是在哪？', '["学校","餐厅","公园","车站"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次见面是在哪？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次一起吃饭吃的是？', '["火锅","面条","快餐","小吃街"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次一起吃饭吃的是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次看的电影是？', '["喜剧","爱情","动画","悬疑"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次看的电影是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次去的地方是？', '["游乐场","海边","公园","商场"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次去的地方是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最常去的地方是？', '["奶茶店","食堂","图书馆","操场"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最常去的地方是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最常一起做的事是？', '["吃饭","散步","打游戏","看剧"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最常一起做的事是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最难忘的一次约会是？', '["第一次约会","生日那次","下雨那次","最近一次"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最难忘的一次约会是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次吵架是因为？', '["小事","误会","游戏","吃饭"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次吵架是因为？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次和好是谁先说话？', '["我","TA","一起","记不清了"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次和好是谁先说话？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次互道晚安是在？', '["微信","电话","面对面","游戏里"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次互道晚安是在？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次送礼物送的是？', '["花","吃的","手写信","小玩偶"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次送礼物送的是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们第一次收到对方礼物是什么感觉？', '["很开心","很惊喜","很感动","有点不好意思"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们第一次收到对方礼物是什么感觉？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最喜欢的合照是哪张？', '["第一张","最近一张","最搞笑那张","最正式那张"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最喜欢的合照是哪张？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最常说的口头禅是？', '["好饿","好困","好喜欢你","随便"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最常说的口头禅是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最想重温的一次约会是？', '["第一次","生日","纪念日","上一次"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最想重温的一次约会是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最想一起去的地方是？', '["海边","雪山","古镇","大城市"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最想一起去的地方是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最想一起做的疯狂的事是？', '["通宵","旅行","蹦极","一起创业"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最想一起做的疯狂的事是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们在一起最开心的时候是？', '["刚在一起","每天","过节","约会时"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们在一起最开心的时候是？');

INSERT INTO pd_quiz_question (content, options)
SELECT '你们最想对对方说的一句话是？', '["我喜欢你","谢谢你","辛苦了","永远在一起"]'
WHERE NOT EXISTS (SELECT 1 FROM pd_quiz_question WHERE content = '你们最想对对方说的一句话是？');
