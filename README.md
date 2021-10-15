# NotesApp
### Background
一个练手Demo。<br>
主要涉及技术Material Design、Activity与Fragment、CoroutineScope、RecycleView、广播、权限、运用手机多媒体、持久化room、网络技术OKHttp等。<br>
### Function
1. 用户模块
* 登录/注册/记住密码
* 用户session管理，Cookie持久化
2. Note模块
* add note
note内可添加：大标题、小标题、内容、图片、链接,可更改note背景色
* updtate note
* delete note
* 主界面根据标题搜索note 
3. 用户模块与后端交互
后端程序部署在VPS服务器。
* 注册新用户同步到后端
* 用户登录与后端同步
* 执行add note， 同步存储在后端数据库
* 执行update note，同步更新后端数据库
* 执行delete note, 同步更新后端数据库
