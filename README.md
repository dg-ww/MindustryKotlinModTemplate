# Mindustry Kotlin 模组模板

一个基于 Kotlin 的 Mindustry 模组，支持在 Android 和 PC 上运行。该模板与 [Java 版本](https://github.com/Anuken/ExampleJavaMod) 等效，只是使用 Kotlin 编写。

## 用于桌面测试的构建

1. 安装 JDK 17。
2. 运行 `gradlew jar` [1]。
3. 你的模组 jar 文件将位于 `build/libs` 目录中。**此版本仅可用于桌面端测试，不适用于 Android。**
   要构建兼容 Android 的版本，你需要 Android SDK。你可以让 GitHub Actions 代为处理，也可以自行配置。具体步骤见下文。

## 通过 GitHub Actions 构建

本仓库已配置好 GitHub Actions CI，每次提交时都会自动为你构建模组。显然，这需要有一个 GitHub 仓库。
要获得适用于所有平台的 jar 文件，请执行以下操作：
1. 创建一个以你的模组名称命名的 GitHub 仓库，并将本仓库的所有内容上传到其中。进行必要的修改，然后提交并推送。
2. 在仓库页面的 "Actions" 选项卡中，选择列表中最新的提交记录。如果构建成功完成，在 "Artifacts" 部分会有一个下载链接。
3. 点击下载链接（文件名应为你的仓库名）。将其中包含的 jar 文件导入 Mindustry。此版本应能在 Android 和桌面端正常运行。

## 本地构建

本地构建需要更多时间进行配置，但如果你有过 Android 开发经验，应该不会有什么问题。
1. 下载 Android SDK，解压并将 `ANDROID_HOME` 环境变量指向其位置。
2. 确保已安装 API 级别 30，以及任意较新版本的构建工具（例如 30.0.1）。
3. 将 build-tools 目录添加到你的 PATH 中。例如，如果你安装了 `30.0.1` 版本，则路径为 `$ANDROID_HOME/build-tools/30.0.1`。
4. 运行 `gradlew deploy`。如果所有步骤都正确完成，这将在 `build/libs` 目录中生成一个可在 Android 和桌面上运行的 jar 文件。

---

*[1]* *在 Linux/Mac 上应使用 `./gradlew`，但如果你在用 Linux，我想你应当知道如何正确运行可执行文件。*