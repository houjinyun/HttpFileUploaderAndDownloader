##关于
    仅用于Android上开发关于文件上传、下载功能的开发工具包，采用了OkHttp作为http底层请求处理。
    Licence: Apache-2.0

##功能
    1.实现了多任务文件上传功能
    2.可以监听文件上传进度
    3.能够十分方便的在ListView等类似控件里显示文件上传进度
    4.对文件上传任务能做个性化的配置：比如上传图片时，能对图片做预处理，限定图片大小，处理图片的旋转角度等
    5.多任务文件下载功能
    6.监听文件下载进度等
    7.基本的http请求工具包，如post请求、get请求

##Gradle
    compile 'com.squareup.okhttp:okhttp:2.5.0'
    compile 'com.hjy.library:FileUploaderAndDownloader:1.0.7'

##使用方法

####一.基本的http请求方法

__使用HttpUtil.java类__


```
/**  
 * 进行get请求  
 *  
 * @param url 请求地址  
 * @param tag 标识该请求  
 * @return  
 */
public static String get(String url, String tag)  

/**  
 * 进行post请求，提交key-value键值对参数  
 *  
 * @param url 请求地址  
 * @param params 表单参数  
 * @param tag 标识该请求  
 * @return  
 */  
public static String post(String url, Map<String, String> params, String tag)  

/**  
 * post提交json格式的数据  
 *  
 * @param url 地址  
 * @param postJsonBody 提交的json格式数据  
 * @param tag 标识该请求  
 * @return  
 */  
public static String post(String url, String postJsonBody, String tag)  

/**  
 * 取消某个请求  
 *  
 * @param tag 标识  
 */
public static void cancelRequest(String tag)  

```

####二.使用文件上传功能（采用multipart/form-data请求上传）
* 在Application的onCreate()方法里进行初始配置，示例代码：

  ```
  FileUploadConfiguration fileUploadConfiguration = new  FileUploadConfiguration.Builder(this)
				.setResponseProcessor(...)  //设置http response字符串的结果解析器，如果不设置，则默认返回response字符串  
                .setThreadPoolSize(5)		 //设置线程池大小，如果采用默认的线程池则有效  
                .setThreadPriority(Thread.NORM_PRIORITY - 1)  //设置线程优先级，如果采用默认的线程池则有效    
                .setTaskExecutor(...)     //设置自定义的线程池
                .setFileUploader(...)     //设置自定义的文件上传功能，如果不设置则采用默认的文件上传功能
                .build();
  FileUploadManager.getInstance().init(fileUploadConfiguration);
  ```

* 实现自己的ResponseParser，一般文件通过http上传成功后，我们会得到一个response，这个response可以是任意格式的字符串，比如json格式、xml格式，我们需要对该字符串进行解析，来判定上传是否真正成功。    
前一步初始化过程中，通过__setResponseProcessor()__设置一个全局的response parser。  
需要继承__BaseResponseParser.java__类，以下是默认提供的response parser  
 
  ```
  public class StringResponseParser extends BaseResponseParser {  
      public StringResponseParser() {  
      }  

      public ParserResult<String> process(final String responseStr) throws Exception {  
    	//这里可能需要对responseStr进行解析生成相应的数据对象，该对象会在上传成功后回调返回
    	Object resultData = responseStr;  //
        ParserResult result = new ParserResult(responseStr) {  
        	//判断是否上传成功
            public boolean isSuccessful() {  
                return true;  
            }  

			//如果没成功，则返回错误结果
            public String getMsg() {  
                return null;  
            }
        };  
        return result;  
      }  
  }  

  ```

  注意到ParserResult.java是个泛型类，这里需要根据responstStr解析出相对应的数据对象，如果判断上传是成功的，该数据对象会在__OnUploadListener.java__里通过onSuccess()方法返回。
  
  ```    
  /**
   * 上传成功
   *
   * @param uploadData
   * @param resultData 数据返回的解析结果，对应ResponseParser里ParserResult构造函数传入的数据对象
   */
  public void onSuccess(FileUploadInfo uploadData, Object resultData);
  ```  

  
  
* 通过__FileUploadManager.java__类上传文件  
其主要调用方法如下：

  ```
  /**
   * 上传图片，系统会根据id、filePath，来唯一标识一个上传任务，如果再次提交则不会创建新的上传任务
   *
   * @param progressAware  如果上传时需要显示上传进度，则传入该参数
   * @param paramMap 文件上传时需要额外提交的参数，没有则不传
   * @param id 需为本次上传任务定一个全局唯一的id，用来标识该上传任务，如果已经有一个同样id、filePath的上传任务，则再提交不会创建新的上传任务
   * @param filePath 需要上传的文件路径
   * @param mimeType  文件的MIME TYPE
   * @param url 上传的url
   * @param apiCallback  文件上传监听器，上传成功或者失败回调，不需要则不传
   * @param uploadProgressListener  文件上传进度监听，不需要则不传
   */
  public void uploadFile(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, OnUploadProgressListener uploadProgressListener, UploadOptions options)  
  ```
  其他重载的方法如下：

  ```
  public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback)  
  
  public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, UploadOptions options)  

  public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, OnUploadProgressListener uploadProgressListener, UploadOptions options)  

  public void uploadFile(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, UploadOptions options)
  ```
  
  同步方法：
   
  ```
  public Object uploadFileSync(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadProgressListener uploadProgressListener, UploadOptions options)  

  ```
  
* __UploadOptions.java__类说明  
前面第一步的配置，是作为一个全局的配置，但是在实际应用当中，可能会出现一些特殊的文件上传功能。比如如下2种情况：   

  __1.__ http上传文件成功后，返回的response字符串格式与其他不一样，需要对这种情况做单独特殊的处理。  
  __2.__ 当我们上传一张图片时，可能需要对原图做一些预处理。例如原图很大，我们需要将其变小，如果原图有旋转，我们需要将其变成一张纠正后的图片再上传。再就是我们可能需要截取原图的一部分上传等等。  
  __3.__ 上传其他类型文件时，根据需要上传处理后的文件。  
  
  示例代码如下：
  
  ```
  File cacheDir = new File(...);		
  UploadOptions options = new UploadOptions.Builder()
      .setPreProcessor(new ImagePreProcessor(cacheDir, 1280, 720))  //ImagePreProcessor是系统提供的图片预处理器，将原图处理成一个不超过最大尺寸的图片再上传
      .setResponseParser(...)  //设置特定的response parser，如果这里有设置则会优先采用，没有则会采用全局设置里的response parser
      .build();
  ```
  
* 更新文件上传进度__（FileUploadManager.java）__  

  我们可能会碰到这样的需求场景，在某个ListView里有多个文件在上传，并且需要分别显示每个上传任务的上传进度。更进一步的是，我们在后台上传多个文件，用户可以随时查看上传进度。  
  前面提交上传任务的时候已经说明，我们是通过__id、filePath__来唯一确定一个上传任务的，也就是我们可以通过这2个字段来查找正在执行的上传任务，并更新进度条的进度
  
  ```
  //如果没有更新进度时，发现上传任务已完成，该方法会隐藏进度条
  public void updateProgress(String id, String filePath, ProgressAware progressAware)  
  
  //如果没有更新进度时，发现上传任务已完成，会显示一个默认的进度值，不会隐藏进度条
  public void updateProgress(String id, String filePath, ProgressAware progressAware, int defProgress)

  ```
  
####三.使用文件下载功能
* 在Application的onCreate()方法里配置：

  ```
  DownloadConfiguration downloadConfiguration = new DownloadConfiguration.Builder(getApplicationContext())
				.setCacheDir(...)        //设置下载缓存目录，必须设置
                .setTaskExecutor(...)    //同上传类似
                .setThreadPriority(...)  //同上传类似
                .setThreadPoolCoreSize(...)  //同上传类似
				.build();
  downloadManager.init(downloadConfiguration);
  ```
* 下载文件（DownloadManager.java）

	```
	/**
     * 下载文件
     *
     * @param type FileType
     * @param id 任务id，自己生成，必须保证唯一
     * @param url 下载地址
     * @param downloadingListener 下载结果回调
     * @param downloadProgressListener 下载进度监听
     */
    public void downloadFile(int type, String id, String url, ProgressAware progressAware, OnDownloadingListener downloadingListener, OnDownloadProgressListener downloadProgressListener)

	```
	
    同步方法
	
	```
	public File downloadFileSync(File cacheFile, String id, String url, ProgressAware progressAware, OnDownloadProgressListener progressListener)

	```
	
* 更新下载进度  
  在某些情况下，我们在后台进行下载任务，可能需要随时查看下载进度。
	
  ```
  public void updateProgress(String id, String url, ProgressAware progressAware)

  ```	