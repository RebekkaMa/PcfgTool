pcfg_tool:  ./src/commonMain/kotlin
	./gradlew nativeBinaries
	cp ./build/bin/native/releaseExecutable/PcfgTool.kexe ./pcfg_tool