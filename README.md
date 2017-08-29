# generate-builder-plugin

---

## Java的builder模式是Java程序员在开发过程中最常用的一种设计模式。但是在开发过程中，由于Builder的参数和原model的参数相同，因此会带来很多重复的工作。generate-builder-plugin是一款专门生成与原model相对应modelBuilder类的插件，只需要指定demo的类名即可自动生成builder类。

---

### version

1.0-SNAPSHOT 

---

### 系统要求

+ maven 3.0

---

### 使用方法

1. git clone https://github.com/lanxing/generate-builder-plugin.git
2. 进入项目
3. chmod +x deploy.sh
4. 运行./deploy.sh
5. 进入需要生成builder的maven模块(如果工程有多模块则需要进入子模块),运行 mvn com.lanxing.plugin:builder-maven-plugin:1.0-SNAPSHOT:touch -Decho.class=com.lanxing.plugin.model.Demo 将Decho.class修改为需要生成builder的类


### 示例

```java
public class Demo {

    private Integer demoId;

    private String demoName;

    private Long demoPrice;

    public Integer getDemoId() {
        return demoId;
    }

    public void setDemoId(Integer demoId) {
        this.demoId = demoId;
    }

    public String getDemoName() {
        return demoName;
    }

    public void setDemoName(String demoName) {
        this.demoName = demoName;
    }

    public Long getDemoPrice() {
        return demoPrice;
    }

    public void setDemoPrice(Long demoPrice) {
        this.demoPrice = demoPrice;
    }
}
```

生成的对应builder:

```java
/**
 * generate by builder-plugin
 */
public class DemoBuilder {

	/**
	 * generate by builder-plugin Demo.demoId
	 */
	private Integer demoId;

	/**
	 * generate by builder-plugin Demo.demoName
	 */
	private String demoName;

	/**
	 * generate by builder-plugin Demo.demoPrice
	 */
	private Long demoPrice;

	public DemoBuilder setDemoId(Integer demoId) {
		this.demoId = demoId;
		return this;
	}

	public DemoBuilder setDemoName(String demoName) {
		this.demoName = demoName;
		return this;
	}

	public DemoBuilder setDemoPrice(Long demoPrice) {
		this.demoPrice = demoPrice;
		return this;
	}

	/**
	 * generate by builder-plugin
	 * @return {@link com.lanxing.boot.service.model.Demo}
	 */
	public Demo build() {
		Demo demo = new Demo();
		demo.setDemoId(this.demoId);
		demo.setDemoName(this.demoName);
		demo.setDemoPrice(this.demoPrice);
		return demo;
	}
}
```