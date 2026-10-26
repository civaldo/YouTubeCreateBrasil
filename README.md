# Criador de Vídeos BR

Aplicativo Android inicial para selecionar e reproduzir vídeos, com construção automática do APK pelo GitHub Actions.

## Estado atual
Esta é uma versão inicial (MVP): permite escolher um vídeo do aparelho e reproduzi-lo dentro do app. Ainda não é um editor completo como o YouTube Create.

## Gerar o APK
1. Abra a aba **Actions** deste repositório.
2. Se necessário, habilite GitHub Actions.
3. Execute o workflow **Construir APK Android** com **Run workflow**.
4. Quando terminar, abra a execução e baixe o artefato **CriadorDeVideosBR-debug-apk**.
5. Extraia o ZIP baixado e instale `app-debug.apk` no Android. Talvez seja necessário permitir a instalação pelo navegador/gerenciador de arquivos.

O APK gerado é de depuração e não é assinado para publicação na Play Store.
