version = version("1.0.0")

dependencies {
    testImplementation(project(":common:addons:manifest-addon-loader"))
    compileOnlyApi(project(":common:addons:manifest-addon-loader"))
}
