# Business Model: Beverage Crop Plantation Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0127`
- ISIC Rev. 4: `0127`
- Industry: Growing of beverage crops
- Social impact: food-security, rural-employment, agricultural-sustainability

## Customer

- Small-to-medium coffee, tea, cacao, and yerba mate growers
- Beverage-crop plantation management companies
- Cooperative mills/processors needing grower-side records
- Contract-farming operations coordinating multiple smallholder plantations

## Offer

- Plantation/block record-keeping (planting, harvest yield, cupping score, leaf grade)
- Field-operation coordination (pruning/spraying/harvest scheduling)
- Crop health and pest/disease tracking (e.g. coffee borer, leaf rust, drought-stress)
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-hectare-per-month pricing)
- Supply chain integration fees
- API access for agronomist partners
- Data analytics and reporting add-ons

## Trust Controls

- No direct field-equipment operation without human sign-off
- No finalizing spray-application decisions
- All field-operation recommendations are proposals, not commands
- Plantation/block registration is required before any operation
- All crop health concerns are automatically escalated
- High-cost supply orders require approval
- Audit ledger is append-only and never editable

## What we NOT do

- **Spray-application decisions** — the agronomist/grower decides application
- **Crop health/welfare decisions** — the grower decides response actions
- **Economic decisions** (harvest timing, replanting) — remain human authority
- **Direct field-equipment operation** — the robot manages records and logistics only

## Supported Operations

### Plantation Record Logging
- Planting counts and block layout
- Harvest weight and yield tracking
- Cupping score (coffee) / leaf grade (tea) quality testing
- Health status notes (logging only, not decision-making)

### Field-Operation Coordination
- Schedule pruning, spraying, harvest
- Track completed field-operation results
- Propose follow-up field work (not order it directly)

### Crop Health Concern Escalation
- Flag suspected pest pressure (e.g. coffee borer)
- Report disease (e.g. leaf rust) or drought-stress observations
- Automatic escalation to grower/agronomist

### Supply Procurement
- Seedling orders
- Fertilizer orders
- Equipment procurement
- Cost threshold escalation for large orders
