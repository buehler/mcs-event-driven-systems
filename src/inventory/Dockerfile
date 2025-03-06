FROM bufbuild/buf:1.50 AS proto_builder
WORKDIR /src

COPY ./buf* ./
COPY ./src ./src

RUN buf generate


FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY --from=proto_builder /src/src/inventory .

RUN dotnet publish \
    --configuration Release \
    -p:PublishSingleFile=true \
    -p:PublishReadyToRun=true \
    -p:DebugType=None \
    -p:DebugSymbols=false \
    -o ./artifacts \
    Inventory/Inventory.csproj


FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app
ENV DOTNET_RUNNING_IN_CONTAINER=true \
    DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=false \
    DOTNET_EnableDiagnostics=0 \
    ASPNETCORE_ENVIRONMENT=Production \
    ASPNETCORE_URLS=http://+:3000 \
    LC_ALL=en_US.UTF-8 \
    LANG=en_US.UTF-8 \
    TZ=Europe/Zurich

COPY --from=build /src/artifacts .

CMD [ "/app/Inventory" ]
