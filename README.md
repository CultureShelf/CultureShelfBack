# CultureShelf Back

API générique de gestion de collections culturelles, partagée par plusieurs fronts
(ex. [MangaReversible](../MangaReversible) pour les mangas collector).

Aucune API publique ne référence les éditions collector. Le back combine donc :

- **un catalogue externe** (fournisseur pluggable, aujourd'hui [MangaDex](https://api.mangadex.org/docs/)) pour
  les séries, auteurs, tomes et couvertures officielles ;
- **nos propres données** par-dessus : variantes d'un tome (collector, jaquette alternative, jaquette réversible,
  exclusivité salon…), leurs visuels, et l'état de la collection.

## Modèle

```
Series (importée d'un fournisseur ou créée à la main)
 └─ Volume (tome)
     └─ Variant (édition physique : STANDARD, COLLECTOR, ALTERNATE_COVER, REVERSIBLE_JACKET…)
         ├─ VariantImage (visuels : FRONT, BACK, SPINE, REVERSE, EXTRA…, uploadés ou par URL)
         └─ CollectionItem (OWNED / PREORDERED / WISHLIST, état, prix payé…)
```

À l'import, chaque tome reçoit une variante « Édition standard ». Une resynchronisation ajoute les nouveaux tomes
sans toucher aux données collector.

## API (`/api/v1`)

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/catalog/providers` | Fournisseurs disponibles |
| GET | `/catalog/search?source=MANGADEX&q=…` | Recherche externe (indique si la série est déjà importée) |
| POST | `/series/import` `{source, externalId}` | Importe (ou resynchronise) une série et ses tomes |
| GET / POST | `/series` | Liste avec compteurs de collection / création manuelle |
| GET / PUT / DELETE | `/series/{id}` | Détail complet (tomes, variantes, visuels, collection) |
| POST | `/series/{id}/refresh` | Resynchronise avec le fournisseur |
| POST | `/series/{id}/volumes` | Ajoute un tome manuellement |
| PUT / DELETE | `/volumes/{id}` | |
| POST | `/volumes/{id}/variants` | Ajoute une variante collector |
| PUT / DELETE | `/variants/{id}` | |
| POST | `/variants/{id}/images` | Visuel : multipart (`file`, `kind`, `caption`) ou JSON `{url, kind, caption}` |
| DELETE | `/images/{id}` | |
| GET | `/collection?status=OWNED` | Vue à plat de la collection |
| PUT / DELETE | `/collection/variants/{variantId}` | Ajoute / met à jour / retire une variante de la collection |
| GET | `/files/{name}` | Sert les visuels uploadés |

Les erreurs sont renvoyées au format `application/problem+json`.

## Lancer

**En local** (JDK 25, base H2 dans `./data`) :

```sh
./mvnw spring-boot:run
```

**Avec Docker** (PostgreSQL + volume pour les visuels) :

```sh
docker compose up --build
```

L'API écoute sur http://localhost:8090.

## Configuration

| Propriété | Défaut | |
|---|---|---|
| `cultureshelf.cors.allowed-origins` | `http://localhost:4200` | Fronts autorisés (liste séparée par des virgules) |
| `cultureshelf.storage.path` | `./data/uploads` | Dossier des visuels uploadés |
| `cultureshelf.providers.mangadex.preferred-locales` | `fr,en,ja` | Langue préférée des titres et couvertures |
| `cultureshelf.providers.mangadex.enabled` | `true` | |

## Ajouter un fournisseur

Implémenter `CatalogProvider` (`source()`, `search`, `getSeries`, `getVolumes`) dans un bean Spring : il est
automatiquement exposé dans `/catalog/providers` et utilisable à l'import.
