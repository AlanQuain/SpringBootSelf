okhttp是一款由square公司开源的java版本http客户端工具。实际上，square公司还开源了基于okhttp进一步封装的retrofit工具，用来支持通过接口的方式发起http请求。

如果你的项目中还在直接使用RestTemplate或者okhttp，或者基于它们封装的HttpUtils，那么你可以尝试使用Retrofit。
retrofit-spring-boot-starter实现了Retrofit与spring-boot框架快速整合，并且支持了部分功能增强，从而极大的简
化spring-boot项目下http接口调用开发。接下来我们直接通过retrofit-spring-boot-starter，来看看spring-boot项目发送http请求有多简单。