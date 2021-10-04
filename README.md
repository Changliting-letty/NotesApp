# NotesApp
 1、已经实现的功能
1）登录、注册
2）写笔记： 大小标题、内容、笔记内可加图片、笔记可修改背景颜色

2、用到书上的技术
1）Material Design
  MaterialCardView 卡片布局显示已经做的笔记
  FloatingActionButton 创建新笔记
2）Activity与Fragment
  实现布局之间来回自由切换
 activity中动态加载fragment
3）CoroutineScope协程
操作数据库--存储笔记、登录验证、用户注册
4）RecycleView 
5）广播
  笔记插入图片、更改颜色
6）持久化
使用room

3、接下来完善：
     1）自动识别是在pad上还是在手机上，动态切换布局
     2）sharedfreference实现记住密码功能
     3）强制下线功能
     4）修改Note的功能
     5）添加通知的功能
