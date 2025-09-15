#!/bin/bash
# Limpa diretório de saída
rm -rf out
mkdir -p out

# Compila o projeto
echo "compilando..."
find src -name "*.java" -print0 | xargs -0 javac -d out -sourcepath src -cp "resources:."

if [ $? -eq 0 ]; then
   
    if [ -f "out/view/ChessGUI.class" ]; then
        echo "🎮 Execute com: java -cp out:resources view.ChessGUI"
    else
        exit 1
    fi
else
    echo "❌ Erro na compilação!"
    exit 1
fi