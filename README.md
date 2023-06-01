# Bitespeed

## Requirement
https://bitespeed.notion.site/Bitespeed-Backend-Task-Identity-Reconciliation-53392ab01fe149fab989422300423199

## API Reference

#### Identify customers endpoint 

```http
  POST https://bitespeed.up.railway.app/api/v1/identify
```
| Parameter | Type | Description | constrain |
| :-------- | :--- | :---------- | :---------|
| `email`      | `String` | `email of customer ` |`NUllable`  |
| `phoneNumber`|`String` |`phone no of customer` |`NUllable`  |
