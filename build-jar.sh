#!/bin/bash

# Limpar compilações anteriores
rm -rf out build *.jar

# Compilar o projeto
./build.sh

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação!"
    exit 1
fi

# Criar estrutura temporária
mkdir -p build/temp/META-INF

# Copiar arquivos compilados
cp -r out/* build/temp/

# Copiar recursos (imagens das peças)
if [ -d "resources" ]; then
    cp -r resources/* build/temp/
fi

# Criar manifest
cat > build/temp/META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: view.ChessGUI
Class-Path: .

EOF

# Gerar JAR
cd build/temp
jar cfm ../../ChessGame.jar META-INF/MANIFEST.MF .
cd ../..

# Verificar se JAR foi criado
if [ -f "ChessGame.jar" ]; then
    echo "size: $(du -h ChessGame.jar | cut -f1)"
else
    echo "erro!"
    exit 1
fi

# Limpar arquivos temporários
rm -rf build/

echo "Sucesso!"
