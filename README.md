# PetPass — assinatura e agendamento de serviços para pets

App Android (Kotlin + Jetpack Compose + Firebase (Auth, Firestore, Storage, Functions)), modelo "Wellhub" para petshops.

## Como abrir
1. Android Studio (Ladybug 2024.2+ ou mais novo) → **Open** → pasta `PetPass`.
2. Aguarde o Gradle sync (usa Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21; precisa de internet).
3. Rode em um emulador/aparelho com Android 8.0+ (minSdk 26).

## Arquitetura
```
br.com.petpass
├── PetApp.kt / MainActivity.kt
├── AppConfig.kt                 # DEMO_MODE e regras de check-in
├── data/
│   ├── Models.kt                # Modelos Firestore: User, Pet, Subscription, Appointment
│   ├── Catalog.kt               # Planos, serviços e petshops parceiros (dados locais)
│   ├── PetRepository.kt         # Auth + Firestore + Storage + Functions
├── notifications/               # Lembretes (AlarmManager + notificação)
├── util/                        # GPS/Geocoder, QR Code, formatação
└── ui/
    ├── ViewModels.kt            # AuthViewModel e MainViewModel
    ├── navigation/AppNavigation.kt
    ├── components/Common.kt     # TopBar, Avatar, Dropdown, seletor de foto
    └── screens/                 # Login/2FA/Cadastro, Pet/Planos/Checkout, Home,
                                 # Busca, Agendamento, Lembrete, Check-in, Perfil
```

## Como cada regra da especificação foi atendida
- **Sessão persistente:** `SessionStore` guarda o token; o app abre direto na Home e só volta ao login após "Sair".
- **2FA por e-mail:** login pede e-mail+senha e depois um código de 6 dígitos. O envio é simulado (`LogEmailSender`); em `DEMO_MODE` o código aparece na tela.
- **Pet:** o dropdown de porte só aparece (e só é exigido) quando a espécie é Cachorro.
- **Plano por pet:** tabela `subscriptions` com índice único em `petId`; trocar de plano substitui a assinatura.
- **Checkout simulado:** Pix (QR dinâmico + copia e cola) e cartão; tela de sucesso com plano e endereço base.
- **Busca:** lista todos os petshops por distância (GPS ou CEP/endereço). Se o plano do pet não cobre o local, abre o modal de **upgrade**.
- **Lembretes:** no dia (8h), 1h ou 10 min antes + exportação para a agenda nativa.
- **Check-in:** habilitado no horário e perto do local; abre a câmera para ler o QR do petshop e incrementa os check-ins do pet.

## Testando o check-in
Abra `web/checkin-qr.html` no navegador do computador, escolha o petshop do agendamento e aponte a câmera do app para a tela.
Com `AppConfig.DEMO_MODE = true` (padrão) horário e distância não bloqueiam o botão; coloque `false` para as regras reais.

## Observações
- **Firebase:** siga `FIREBASE_SETUP.md` (google-services.json, regras e Cloud Functions do 2FA em `firebase/`).
- O Geocoder (busca por CEP) precisa de internet.
- Código não foi compilado neste ambiente (sem acesso à rede para baixar dependências); se o Gradle apontar algum erro, me envie a mensagem que eu corrijo.
