pcfg_tool:  ./src/commonMain/kotlin
	./gradlew nativeBinaries
	cp ./build/bin/native/releaseExecutable/Praktikum.kexe ./pcfg_tool