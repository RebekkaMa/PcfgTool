pcfg_tool:  ./src/main/*
	./gradlew installDist
	cp -r ./build/install/PcfgTool/* ./
	mv PcfgTool pcfg_tool
	rm PcfgTool.bat