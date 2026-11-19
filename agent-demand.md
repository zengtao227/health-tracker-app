# Agent Demand Gate: Codex model routing

## 1. Friction Point
- Current user friction: implementation, planning, and review roles are not consistently using the requested models and reasoning levels.
- Who experiences it: the repository owner when delegating coding and reviewing changes in Codex.
- Why fixed rules or normal automation are insufficient: the routing is deterministic and does not require an autonomous product Agent; existing Codex role configuration is sufficient.
- Evidence source: `~/.codex/config.toml`, `~/.codex/agents/luna-worker.toml`, and `~/.codex/agents/reviewer.toml` on 2026-08-08.

## 2. Quantified Gap
- Baseline metric: 2 explicit role mismatches: the default implementation route used Luna instead of Terra, and reviewer used GPT-5.5 instead of GPT-5.6 Sol.
- Target metric: 0 role mismatches against the requested routing.
- Failure or exit point: planning or review does not use Sol/high, routine implementation does not default to Terra/medium, or a complex implementation is not escalated to Terra/high.
- Acceptable error / misclassification rate: 0 for explicitly selected worker and reviewer roles.
- Measurement window: verify immediately after configuration changes and recheck after the next Codex restart/new task.

## 3. Solution Choice
- Recommended path: non-agent-automation
- Why this path fits current data and change frequency: existing static TOML routing fully expresses the requested model policy.
- Why the rejected paths are weaker: prompt chains, workflow orchestration, and fine-tuning add no value to a deterministic role-to-model mapping.
- Smallest useful prototype: route default implementation to Terra/medium, keep Terra/high for explicit complex implementation, keep Luna/xhigh as an explicit mechanical-task option, and route review to Sol/high.

## 4. Success Preview And Risk Plan
- Success standard: main planning and reviewer use GPT-5.6 Sol/high; default implementation uses GPT-5.6 Terra/medium; explicit complex implementation uses Terra/high; Luna/xhigh remains available only for explicit mechanical tasks.
- Pause / kill signal: Codex fails to load Terra/medium, Terra/high, or the role files.
- Degraded fallback: use Sol/high for implementation while keeping the same planning and review policy.
- Owner and review cadence: repository owner; review after Codex restart and whenever model availability changes.
