### Build
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build

WORKDIR /app

COPY . ./

RUN dotnet publish \
  --configuration Release \
  -p:PublishSingleFile=true \
  -p:PublishReadyToRun=true \
  -p:DebugType=None \
  -p:DebugSymbols=false \
  -o ./artifacts \
  ./Consumer.csproj

### Deploy
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final

WORKDIR /app

COPY --from=build /app/artifacts .

ENV DOTNET_RUNNING_IN_CONTAINER=true \
  DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=false \
  DOTNET_EnableDiagnostics=0 \
  ASPNETCORE_ENVIRONMENT=Production \
  LC_ALL=en_US.UTF-8 \
  LANG=en_US.UTF-8 \
  TZ=Europe/Zurich

CMD [ "/app/Consumer" ]
