# tastytrade API Intro

New users should start by following the steps below. They will walk you through some of the key features of the tastytrade API such as logging in, submitting an order, viewing your positions, and closing a position.

For an overview of API rules and patterns, you can head to the API Overview page.

For an overview of some core tastytrade concepts, head to the High-level Concepts section of our API Overview.

If you just want to see the docs, head to the API Docs page.

## Environments
We have 2 environments you can use: Sandbox and Production. Each environment is separate, meaning you will have different credentials for each.

Sandbox - a test environment where everything is fake and you can test your applications before putting real money to use. Head to the Sandbox page for more information about this environment.
URL: https://api.cert.tastyworks.com
Production - our live environment where everything is real and trades do matter. You can start using this environment when you feel ready. We recommend testing your code in the sandbox environment before hitting the production environment.
URL: https://api.tastyworks.com
Getting Started
1. Create a Sandbox Account
You should start by creating an account in our sandbox environment using the Sandbox page. We have a whole working test environment that you will be able to build your application and test in prior to hooking up your production account.

2. Generate an Oauth2 access token
Every request you make needs an Authorization header with a valid access token.

Head to our Auth Patterns Overview more info about sessions and the Authorization header.

3. Submit a Trade
Submit a trade by hitting our Submit Order endpoint.

You can find detailed instructions about how to structure an order on our Order Submission page.

Our Sandbox environment has custom logic around order submission that makes it easy to simulate an order fill, partial fill, or a live order. Head to our Sandbox Page for more info.

4. Fetch Your Account Balance and Positions
Once your order fills, a new position will be created in your account. Your account balance will also be updated to reflect the cost of the order.

Hit our List Account Positions endpoint to fetch your account positions.

Hit our List Account Balances endpoint to view your account balance.

5. Stream Market Data
Our Streaming Market Data page has all the info you need to fetch quotes. The quotes will be delayed in our Sandbox environment. Production environment quotes are real-time.

6. Fetch Market Data
You can also fetch a quote via http. Our Market Data Guide will get you started.

7. Stream Account Updates
To get real time updates regarding your account, head to our Streaming Account Data page.

8. Close a Position
Closing a position is done by submitting an order in the opposite direction as the position. For example, if you are long 100 shares of AAPL, you can submit a Sell to Close order for 100 shares of AAPL. When this order fills, your position will be zeroed out.

For more information on closing and opening, head to our Leg Attributes section of the Order Submission page.

9. Fetch an Option Chain
If you want to see a full option chain for a ticker symbol, head to our List Nested Option Chains section. This will list every put and call symbol for every expiration that tastytrade supports for the given ticker symbol. It also includes the streamer-symbol, which is the symbol to use when subscribing to quote data from DxLink.

Running into issues or have questions?
Head to our FAQ page to see if your question can be answered there.

We also have a dedicated Service Desk ready to assist you. Please submit an email to api.support@tastytrade.com in order to create a ticket, and a member of our team will be in touch.

---

## tastytrade API Overview
API Conventions (REST / JSON)
Api Versions
Auth Patterns
Access Tokens (Oauth2)
Authorization Header
tastytrade Symbology
Error Codes
High-level Concepts
Rate Limits


API Conventions (REST / JSON)
Headers
User-Agent
All requests must include a User-Agent header or they will be rejected. The format must be <product>/<version>. The product and version you choose does not matter as long as it is submitted in the proper format. Most people make it specific to the client they are using, like tastytrade-api-client/1.0 or my-custom-client/2.0.

Content-Type
Content-Type and Accept headers should be application/json since requests and responses will always be in json format.

Accept-Version
Use the Api-Version header to hit a specific version of our api. The value is always a date in the format YYYYMMDD. You can find out which versions are available in our Api Docs section. See below for an explanation of our Api Versioning strategy.

Parameters
Json keys are dasherized in the request and response body. For example:

{
  "this-key-is-dasherized": "value goes here"
}
GET requests must send parameters in the url (i.e. query parameters). Some GET endpoints have a parameter of type Array[String]. These must be provided in the format my-key[]=value. If you wanted to send multiple values, you would simply repeat the key: my-key[]=value1&my-key[]=value2.

For all other HTTP methods (POST, PUT, PATCH, DELETE), use a json body. Remember to dasherize-the-parameter-names:

POST
/accounts/{account_number}/orders
{
  "underlying-symbol": "AAPL",
  "quantity": 1,
  "time-in-force": "Day"
  // other params here
}
Response Schema
Response json will have a data key and a context key. Multi-object responses will have an items array nested inside the data object:

  {
    "data": {
      "items": [
        /* list of json objects */
      ]
    },
    "context": "/accounts/{account_number}/positions"
  }
Error responses will include an error object with code and message properties:

{
  "error": {
    "code": "not_permitted",
    "message": "User not permitted access"
  }
}


Api Versions
tastytrade will sometimes need to introduce breaking changes to one or more endpoints. To avoid breaking your existing code, tastytrade will deprecate the existing version and add the breaking changes into a new api version.

Api versions are always the date at which the version was introduced. For example, a version introduced on September 4, 2025 will be 20250904.

To target a specific api version in your request, add the Accept-Version header to each request. For example: Accept-Version: 20250904.

When a new API version is introduced, the old versions will be deprecated. Deprecated Api versions will have at least 6 months to live from the time they are deprecated. We announce new API versions and deprecations in our Release Notes section. Please ensure you are monitoring the Release Notes section and update your code to hit the latest version. If you request a version that no longer exists, you'll get an Http 406 error with a response that looks like this:

"error": {
    "code": 406,
    "message": "The requested version is not supported.
}
Each Api Docs page will tell you which versions are available and which are deprecated (see the screenshot below). You'll see a dropdown list at the top of some api docs pages that lets you view each version. Older versions will have one or more deprecated endpoints.

You should also check the Release Notes page for a list of changes introduced with each Api Version.

For example, if you're attempting to hit api version 20250830 but you send Accept-Version: 20240829, you'll receive the above Http 406 error.

If you always want to target the latest api version, you can send today's date in the Accept-Version header. For example, if today were January 1st, 2025, you'd send Accept-Version: 20250101. Tastytrade will always route your request to the nearest Api version at or before your target. For example, suppose we offer 2 Api versions: 20250101 and 20250201. A request with Accept-Version: 20250102 will be routed to the 20250101 version. We will not route to a version greater than your requested version.



Auth Patterns
Requests to the tastytrade API are authenticated via access tokens. The access token is sent via the Authorization header of each request.

Access Tokens (Oauth2)
Access tokens are generated by the POST /oauth/token endpoint. You must have an Oauth client registered with tastytrade in order to use the Oauth endpoints.

For detailed instructions on using the Oauth flow, see our Oauth2 API guide.

Access tokens are valid for 15 minutes and must be refreshed when they expire. Refreshing an access token generates a new token. You cannot extend the life of an existing access token.

Authorization Header
All requests to the tastytrade api are authenticated via the Authorization header on the request. The Authorization header's value is a bearer token, meaning the access token must be prefixed with Bearer . For example:

Authorization: Bearer abc123

Please note that real access tokens are significantly longer than the example above.

Requests that do not include the Authorization header will receive an Http 401 response code (Unauthorized).

Requests with an expired or access token in the Authorization header will also receive an Http 401 response code.

tastytrade Symbology
Equities
Equity symbols contain only alphanumeric characters (A-Z, 0-9) with an occasional /. A few examples: AAPL BRK/A

Equity Options
tastytrade uses the same conventions as the OCC for equity option symbols. In short, there are 4 pieces that make up an equity option symbol:

Root symbol - 6 alphanumeric digits with whitespace padding.
AAPL  
FB    
BRK/A 
Expiration date - 6 numeric digits with format yymmdd.
Option type - P or C
Strike price - 8 numeric digits front-padded with 0s. No decimals. Multiply strike by 1000.
64.0 strike: 00064000
1050.55 strike: 01050550
0.50 strike: 00000500
Example equity option symbols:

AAPL June 17, 2022 150 Put: AAPL 220617P00150000

SPY Nov 18, 2022 400 Call: SPY 221118C00400000

SPX May 20, 2022 4025 Call: SPXW 220520C04025000

Futures
Futures symbols always start with a slash followed by the contract code. The contract code consists of 3 things: the product code (A-Z), the month code, and a 1-2 digit number for the year. For a full list of futures products that we support, hit the List Future Products endpoint. Each item in the response has a code, which is the product code.

E-mini S&P 500 December 2022: /ESZ2

Crude Oil December 2022: /CLZ2

CBOE Volatility Index November 2022: /VXX22

You can find a list of all month codes here.

Future Options
Future options are different from equity options in that each future option expiration series has a unique product code. For example, monthly CL future options have a product code LO. Weekly CL options have product codes LO1, LO2, LO3, LO4, and LO5 where LO1 is the first Friday of the month, LO2 is the second Friday of the month, etc.

Other future products may have weekly options for any day of the week with their own set of option product codes. For example, ES has weekly Monday options with product codes E1A, E2A, E3A, E4A, E5A.

Both the future product code and the future option product code will be present in a future option symbol. The future option symbol will always start with a ./, followed by the future contract code, the option contract code, the expiration date, option type (C/P), and strike price. Here are some examples:

./CLZ2 LO1X2 221104C91

LO1X2 means it's a November weekly option product. CLZ2 means this option settles into the December CL future contract, which expires 2022-11-21. The 221104C91 means it's a Call option at the 91 strike expiring 2022-11-04.

./ESZ2 E1AZ2 221205P3720

The option contract code is E1AZ2. Future contract code is ESZ2. The option product code is E1A, which is a weekly Monday product. So this is a 2022-12-05 Put at the 3720 strike which settles into the December ES Future contract.

For a full list of future option products that we support, hit the List Future Option Products endpoint. Each item in the response has a root-symbol, which is the product code.

Cryptocurrencies
Cryptocurrency symbols can be found via GET /instruments/cryptocurrencies in the symbol field of the response data. Some examples:

BCH/USD - Bitcoin cash

BTC/USD - Bitcoin

Error Codes
400 - Invalid request. Often indicates that your request body has missing or invalid parameters.

401 - Authorization token has expired or is invalid. Also indicates an invalid username/password when logging in.

403 - User is not authorized to access this resource. This may occur when a customer tries to access data for an account belonging to a different customer, for example.

404 - Endpoint or resource not found. This may occur when attempting to fetch data that does not exist (a specific order, for example).

422 - Unprocessable content. This occurs when the action you are performing is invalid, depending on the context. For example, our order system will return a 422 with a message in the response json if your order was rejected.

429 - Too Many Requests. This occurs when you send a high amount of requests in a short period of time to the point where it exceeds reasonable thresholds.

500 - Indicates an issue with tastytrade's servers. Returns a support identifier that our team can use to track down the issue.

High-level Concepts
Orders
Orders are the mechanism by which you trade. They are also how you manage your positions. Each order can have a maximum of 4 legs. Each leg has a symbol, a quantity, and an action.

The action includes both a direction (buy/sell) and an effect (open/close). Open means "Increase my position." Close means "Decrease my position."

In other words, an opening order will increase the quantity of your position (if you don't have a position, a new one will be created when the order fills). A closing order will decrease the quantity of your existing position.

For example, to buy 100 shares of AAPL stock, you would submit an order with a single leg. The leg's symbol would be AAPL. The leg's quantity would be 100. The leg's action would be Buy to Open, meaning you are buying with the intent to open a new position. When this order fills, tastytrade creates a position in your account with a quantity of 100, a direction of Long, and a symbol of AAPL.

If you then decide to sell 10 shares of AAPL stock, you'd submit an order with a single leg. The leg's symbol would again be AAPL. The leg's quantity would be 10. The leg's action would be Sell to Close, meaning you are closing 10 shares of your existing position. When this order fills, tastytrade adjusts your position quantity down by 10, leaving you with 90 long shares.

It is perfectly acceptable to close out your entire position in a single order. If you are long 100 shares of AAPL, you can close them all by submitting an order to Sell to Close 100 shares.

It is perfectly acceptable to submit an opening sell order. Most call this "selling short." This type of order will result in a position with a Short direction. Please note you cannot Sell to Open if you have an existing Long position. Likewise you cannot Buy to Open if you have an existing Short position. Holding both a long and short position is not permitted at tastytrade.

For a detailed overview of how to structure and submit an order, head to the Order Submission page.

Orders have a status once they are submitted. The tastytrade order status lifecycle is explained in detail on the Order Flow page.

Positions
A position is created/adjusted when an order fills. Positions have a symbol, a quantity, and a direction.

You cannot create multiple positions with the same symbol. For example, you cannot simultaneously be long AAPL stock and short AAPL stock. If you are long AAPL stock and submit a sell order, the quantity of your position will decrease until it hits zero. If you continue to sell stock after hitting 0, the direction of your position will flip from long to short. Position quantity is always a positive number.

See our Account Positions Api Guide for more information.

Accounts
Your account is tied to almost every api endpoint. You can open multiple accounts, but each api endpoint will operate on a single account. You cannot submit an order to multiple accounts in a single http request.

See our Customer Account Information Api Guide for more information.

Trading Statuses
Each tastytrade account is linked to a single trading status. The trading status gives information on what privileges your account has, such as futures trading, crypto trading, options trading strategies, etc.

See our Account Trading Status Api Guide for more information.

Balances
Each tastytrade account is linked to a single account balance. The account balance gives current information about cash, buying power, the value of long and short positions, net liquidating value, and other data.

Your account balance is updated when events like order fills and cash deposits occur.

See our Account Balances Api Guide for more information.

Transactions
tastytrade maintains a historical ledger of each account activity that affects your balance. We create a transaction for each of these account activities. Some examples include order fills, dividends, fees, cash deposits, cash withdrawals, etc.

See our Account Transactions Api Guide for more information.

Instruments
An instrument is a single tradeable security, such as an equity, equity option, future, future option, or cryptocurrency. Each has a unique symbol by which it is identified. The symbol is used when submitting an order and when positions are created.

See our Instruments Api Guide for more information.

Rate Limits
tastytrade implements rate limiting to ensure all customers have a responsive and positive experience with the API. These rate limits are set in a way to provide functionality while also trying to prevent abuse to the system. We guide customers to lean on our streaming services to avoid polling too heavily, which often can result in hitting rate limits and 429 error codes. If you do receive 429s please pull back on your request rates or add delays to certain functions.

Streaming Rate Limits
Maximum Total Sessions: 5

Maximum Subscriptions per Session: 25,000

Per Event Specific Subscription Max: 100 for Candle, Order, Series

Subscription Change Rate: 10,000/minute

---

## Api Docs

Account Status
 7.4.0 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
trading-status
Allows an API client to request information about the basic trade status of an account.
This includes information about the strategies an account can trade.



GET
/accounts/{account_number}/trading-status

Models
TradingStatus{
description:	
TradingStatus model

id	integer($int32)
account-number	string
are-deep-itm-carry-options-enabled	boolean
are-far-otm-net-options-restricted	boolean
are-options-values-restricted-to-nlv	boolean
are-single-tick-expiring-hedges-ignored	boolean
autotrade-account-type	string
clearing-account-number	string
clearing-aggregation-identifier	string
cmta-override	integer($int32)
day-trade-count	integer($int32)
enhanced-fraud-safeguards-enabled-at	string($date-time)
equities-margin-calculation-type	string
ext-crm-id	string
fee-schedule-name	string
futures-margin-rate-multiplier	number($double)
has-intraday-equities-margin	boolean
is-aggregated-at-clearing	boolean
is-ascend-event-contracts-enabled	boolean
is-closed	boolean
is-closing-only	boolean
is-cryptocurrency-closing-only	boolean
is-cryptocurrency-enabled	boolean
is-equity-offering-closing-only	boolean
is-equity-offering-enabled	boolean
is-event-contracts-closing-only	boolean
is-frozen	boolean
is-full-equity-margin-required	boolean
is-futures-closing-only	boolean
is-futures-enabled	boolean
is-futures-intra-day-enabled	boolean
is-in-day-trade-equity-maintenance-call	boolean
is-in-margin-call	boolean
is-non-retail	boolean
is-pattern-day-trader	boolean
is-portfolio-margin-enabled	boolean
is-risk-reducing-only	boolean
is-roll-the-day-forward-enabled	boolean
is-small-notional-futures-intra-day-enabled	boolean
options-level	string
pdt-reset-on	string($date)
short-calls-enabled	boolean
small-notional-futures-margin-rate-multiplier	number($double)
updated-at	string($date-time)
}


Accounts and Customers
 10.65.0 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
api-quote-tokens
Operations about api-quote-tokens



GET
/api-quote-tokens
Returns the appropriate API quote streamer endpoint, level and identification token
for the current customer to receive market data.

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Returns the appropriate API quote streamer endpoint, level and identification token
for the current customer to receive market data.

Example Value
Model
{
  "dxlink-url": "string",
  "expires-at": "2026-08-16T07:05:55.700Z",
  "issued-at": "2026-08-16T07:05:55.700Z",
  "level": "string",
  "token": "string",
  "websocket-url": "string"
}
customers
Operations about customers



GET
/customers/{customer_id}
Get a full customer resource.

Parameters
Name	Description
customer_id *
integer($int32)
(path)
customer_id
allow-missing
boolean
(query)
Does not raise a 404 if customer is missing

Default value : false


false
Responses
Response content type

application/json
Code	Description
200	
Get a full customer resource.

Example Value
Model
{
  "has-institutional-assets": "string",
  "visa-expiration-date": "string",
  "gender": "string",
  "second-surname": "string",
  "last-name": "string",
  "political-organization": "string",
  "middle-name": "string",
  "entity": {
    "secretary-name": "string",
    "is-domestic": "string",
    "entity-type": "string",
    "entity-officers": [
      {
        "relationship-to-entity": "string",
        "visa-expiration-date": "2026-08-16",
        "last-name": "string",
        "middle-name": "string",
        "work-phone-number": "string",
        "prefix-name": "string",
        "visa-type": "string",
        "number-of-dependents": "string",
        "suffix-name": "string",
        "job-title": "string",
        "birth-country": "string",
        "first-name": "string",
        "occupation": "string",
        "marital-status": "string",
        "tax-number": "string",
        "citizenship-country": "string",
        "usa-citizenship-type": "string",
        "owner-of-record": true,
        "is-foreign": "string",
        "employment-status": "string",
        "mobile-phone-number": "string",
        "address": {
          "is-domestic": "string",
          "street-two": "string",
          "city": "string",
          "postal-code": "string",
          "state-region": "string",
          "is-foreign": "string",
          "street-three": "string",
          "country": "string",
          "street-one": "string"
        },
        "home-phone-number": "string",
        "id": "string",
        "tax-number-type": "string",
        "email": "string",
        "birth-date": "2026-08-16",
        "employer-name": "string",
        "external-id": "string"
      }
    ],
    "phone-number": "string",
    "grantor-birth-date": "string",
    "has-foreign-bank-affiliation": "string",
    "date-of-trust-creation": "string",
    "tax-number": "string",
    "grantor-email": "string",
    "has-foreign-institution-affiliation": "string",
    "is-hedge-fund": "string",
    "mailing-address": {
      "is-domestic": "string",
      "street-two": "string",
      "city": "string",
      "postal-code": "string",
      "state-region": "string",
      "is-foreign": "string",
      "street-three": "string",
      "country": "string",
      "street-one": "string"
    },
    "entity-suitability": {
      "entity-id": 0,
      "tax-bracket": "string",
      "annual-net-income": 0,
      "liquid-net-worth": 0,
      "stock-trading-experience": "string",
      "futures-trading-experience": "string",
      "uncovered-options-trading-experience": "string",
      "id": "string",
      "covered-options-trading-experience": "string",
      "net-worth": 0
    },
    "grantor-last-name": "string",
    "business-nature": "string",
    "address": {
      "is-domestic": "string",
      "street-two": "string",
      "city": "string",
      "postal-code": "string",
      "state-region": "string",
      "is-foreign": "string",
      "street-three": "string",
      "country": "string",
      "street-one": "string"
    },
    "grantor-middle-name": "string",
    "grantor-first-name": "string",
    "grantor-tax-number": "string",
    "id": "string",
    "tax-election": "string",
    "email": "string",
    "legal-name": "string",
    "foreign-institution": "string"
  },
  "work-phone-number": "string",
  "permitted-account-types": "string",
  "foreign-tax-number": "string",
  "listed-affiliation-symbol": "string",
  "prefix-name": "string",
  "visa-type": "string",
  "suffix-name": "string",
  "birth-country": "string",
  "mobile-phone-number-details": "string",
  "first-name": "string",
  "signature-of-agreement": true,
  "is-professional": true,
  "agreed-to-terms": true,
  "industry-affiliation-firm": "string",
  "subject-to-tax-withholding": true,
  "home-phone-number-details": "string",
  "tax-number": "string",
  "citizenship-country": "string",
  "usa-citizenship-type": "string",
  "has-political-affiliation": true,
  "customer-suitability": {
    "tax-bracket": "string",
    "number-of-dependents": 0,
    "annual-net-income": 0,
    "job-title": "string",
    "customer-id": 0,
    "occupation": "string",
    "marital-status": "string",
    "liquid-net-worth": 0,
    "stock-trading-experience": "string",
    "employment-status": "string",
    "futures-trading-experience": "string",
    "uncovered-options-trading-experience": "string",
    "id": "string",
    "covered-options-trading-experience": "string",
    "employer-name": "string",
    "net-worth": 0
  },
  "identifiable-type": "string",
  "is-foreign": "string",
  "cherry-club-date": "2026-08-16",
  "mailing-address": {
    "is-domestic": "string",
    "street-two": "string",
    "city": "string",
    "postal-code": "string",
    "state-region": "string",
    "is-foreign": "string",
    "street-three": "string",
    "country": "string",
    "street-one": "string"
  },
  "has-listed-affiliation": true,
  "is-investment-adviser": "string",
  "mobile-phone-number": "string",
  "has-industry-affiliation": true,
  "address": {
    "is-domestic": "string",
    "street-two": "string",
    "city": "string",
    "postal-code": "string",
    "state-region": "string",
    "is-foreign": "string",
    "street-three": "string",
    "country": "string",
    "street-one": "string"
  },
  "person": {
    "visa-expiration-date": "2026-08-16",
    "last-name": "string",
    "middle-name": "string",
    "prefix-name": "string",
    "visa-type": "string",
    "number-of-dependents": "string",
    "suffix-name": "string",
    "job-title": "string",
    "birth-country": "string",
    "first-name": "string",
    "occupation": "string",
    "marital-status": "string",
    "citizenship-country": "string",
    "usa-citizenship-type": "string",
    "employment-status": "string",
    "birth-date": "2026-08-16",
    "employer-name": "string",
    "external-id": "string"
  },
  "has-delayed-quotes": true,
  "desk-customer-id": "string",
  "ext-crm-id": "string",
  "home-phone-number": "string",
  "agreed-to-margining": true,
  "created-at": "2026-08-16T07:05:55.703Z",
  "regulatory-domain": "string",
  "work-phone-number-details": "string",
  "id": "string",
  "has-pending-or-approved-application": "string",
  "tax-number-type": "string",
  "email": "string",
  "birth-date": "string",
  "user-id": "string",
  "external-id": "string",
  "family-member-names": "string",
  "first-surname": "string"
}

GET
/customers/{customer_id}/accounts
Get a list of all the customer account resources attached to the current customer.

Parameters
Name	Description
customer_id *
integer($int32)
(path)
customer_id
Responses
Response content type

application/json
Code	Description
200	
Get a list of all the customer account resources attached to the current customer.

Example Value
Model
[
  {
    "account": {
      "day-trader-status": "string",
      "futures-account-purpose": "string",
      "liquidity-needs": "string",
      "ext-account-id": "string",
      "external-accounts": "string",
      "account-number": "string",
      "nickname": "string",
      "suitable-options-level": "string",
      "risk-tolerance": "string",
      "is-closed": true,
      "is-foreign": "string",
      "is-firm-proprietary": true,
      "closed-at": "2026-08-16T07:05:55.711Z",
      "investment-time-horizon": "string",
      "ext-crm-id": "string",
      "submitting-user-id": "string",
      "opened-at": "2026-08-16T07:05:55.711Z",
      "is-futures-approved": true,
      "created-at": "2026-08-16T07:05:55.711Z",
      "regulatory-domain": "string",
      "external-fdid": "string",
      "is-firm-error": true,
      "external-id": "string",
      "funding-date": "2026-08-16",
      "account-type-name": "string",
      "margin-or-cash": "string",
      "investment-objective": "string"
    },
    "authority-level": "string"
  }
]

GET
/customers/{customer_id}/accounts/{account_number}
Get a full customer account resource.

Parameters
Name	Description
customer_id *
integer($int32)
(path)
customer_id
account_number *
integer($int32)
(path)
account_number
Responses
Response content type

application/json
Code	Description
200	
Get a full customer account resource.

Example Value
Model
{
  "day-trader-status": "string",
  "futures-account-purpose": "string",
  "liquidity-needs": "string",
  "ext-account-id": "string",
  "external-accounts": "string",
  "account-number": "string",
  "nickname": "string",
  "suitable-options-level": "string",
  "risk-tolerance": "string",
  "is-closed": true,
  "is-foreign": "string",
  "is-firm-proprietary": true,
  "closed-at": "2026-08-16T07:05:55.713Z",
  "investment-time-horizon": "string",
  "ext-crm-id": "string",
  "submitting-user-id": "string",
  "opened-at": "2026-08-16T07:05:55.713Z",
  "is-futures-approved": true,
  "created-at": "2026-08-16T07:05:55.713Z",
  "regulatory-domain": "string",
  "external-fdid": "string",
  "is-firm-error": true,
  "external-id": "string",
  "funding-date": "2026-08-16",
  "account-type-name": "string",
  "margin-or-cash": "string",
  "investment-objective": "string"
}

Models
QuoteStreamerTokenAuthResult{
description:	
QuoteStreamerTokenAuthResult model

dxlink-url	string
expires-at	string($date-time)
issued-at	string($date-time)
level	string
token	string
websocket-url	string
}
AccountAuthorityDecorator{
description:	
AccountAuthorityDecorator model

account	{...}
authority-level	string
}
Account{
description:	
Account model

account-number	string
account-type-name	string
closed-at	string($date-time)
created-at	string($date-time)
day-trader-status	string
ext-account-id	string
ext-crm-id	string
external-accounts	string
external-fdid	string
external-id	string
funding-date	string($date)
futures-account-purpose	string
investment-objective	string
investment-time-horizon	string
is-closed	boolean
is-firm-error	boolean
is-firm-proprietary	boolean
is-foreign	string
is-futures-approved	boolean
liquidity-needs	string
margin-or-cash	string
nickname	string
opened-at	string($date-time)
regulatory-domain	string
risk-tolerance	string
submitting-user-id	string
suitable-options-level	string
}
Customer{
description:	
Customer model

id	string
first-name	string
first-surname	string
last-name	string
middle-name	string
prefix-name	string
second-surname	string
suffix-name	string
cherry-club-date	string($date)
address	{...}
customer-suitability	{...}
mailing-address	{...}
is-foreign	string
regulatory-domain	string
usa-citizenship-type	string
home-phone-number	string
home-phone-number-details	string
mobile-phone-number	string
mobile-phone-number-details	string
work-phone-number	string
work-phone-number-details	string
birth-date	string
email	string
external-id	string
foreign-tax-number	string
tax-number	string
tax-number-type	string
birth-country	string
citizenship-country	string
visa-expiration-date	string
visa-type	string
agreed-to-margining	boolean
subject-to-tax-withholding	boolean
agreed-to-terms	boolean
signature-of-agreement	boolean
desk-customer-id	string
ext-crm-id	string
family-member-names	string
gender	string
has-industry-affiliation	boolean
has-institutional-assets	string
has-listed-affiliation	boolean
has-political-affiliation	boolean
industry-affiliation-firm	string
is-investment-adviser	string
listed-affiliation-symbol	string
political-organization	string
user-id	string
has-delayed-quotes	boolean
has-pending-or-approved-application	string
is-professional	boolean
permitted-account-types	string
created-at	string($date-time)
entity	{...}
identifiable-type	string
person	{...}
}


Backtester Backend API
 1.0.0 
OAS3
tastytrade options backtester

Servers

https://backtester.vast.tastyworks.com - Backtester API
Filter by tag
default


GET
/backtests
Get user's backtests
Get the IDs of all the current user's backtests

Parameters
No parameters

Responses
Code	Description	Links
200	
Backtest list

Media type

application/json
Example Value
Schema
[
  "string"
]
No links

POST
/backtests
Create backtest
Create new backtest

Parameters
No parameters

Request body

application/json
Example Value
Schema
{
  "symbol": "string",
  "startDate": "2026-08-16",
  "endDate": "2026-08-16",
  "legs": [
    {
      "currentPriceOffset": 50000,
      "quantity": 100,
      "percentageOTM": 0,
      "strikeRelativeLeg": 0,
      "daysUntilExpiration": 0,
      "side": "call",
      "premium": 50000,
      "delta": 100,
      "strikeSelection": "delta",
      "type": "equity",
      "direction": "long"
    }
  ],
  "entryConditions": {
    "frequency": "every day",
    "specificDays": [
      0
    ],
    "maximumActiveTrials": 0,
    "maximumActiveTrialsBehavior": "don't enter",
    "minimumVIX": 0,
    "maximumVIX": 0
  },
  "exitConditions": {
    "takeProfitPercentage": 0,
    "stopLossPercentage": 0,
    "afterDaysInTrade": 0,
    "atDaysToExpiration": 0,
    "minimumVIX": 0
  }
}
Responses
Code	Description	Links
200	
Backtest ready

Media type

application/json
Example Value
Schema
{
  "statistics": [
    {}
  ],
  "legs": [
    {
      "currentPriceOffset": 50000,
      "quantity": 100,
      "percentageOTM": 0,
      "strikeRelativeLeg": 0,
      "daysUntilExpiration": 0,
      "side": "call",
      "premium": 50000,
      "delta": 100,
      "strikeSelection": "delta",
      "type": "equity",
      "direction": "long"
    }
  ],
  "endDate": "2026-08-16",
  "entryConditions": {
    "frequency": "every day",
    "specificDays": [
      0
    ],
    "maximumActiveTrials": 0,
    "maximumActiveTrialsBehavior": "don't enter",
    "minimumVIX": 0,
    "maximumVIX": 0
  },
  "progress": 0,
  "snapshots": [
    {
      "dateTime": "string",
      "profitLoss": 0,
      "underlyingPrice": 0
    }
  ],
  "startDate": "2026-08-16",
  "ETA": 0,
  "status": "pending",
  "exitConditions": {
    "takeProfitPercentage": 0,
    "stopLossPercentage": 0,
    "afterDaysInTrade": 0,
    "atDaysToExpiration": 0,
    "minimumVIX": 0
  },
  "symbol": "string",
  "notices": [
    "string"
  ],
  "id": "string",
  "trials": [
    {
      "openDateTime": "string",
      "closeDateTime": "string",
      "profitLoss": 0
    }
  ]
}
No links
201	
Backtest pending

Media type

application/json
Example Value
Schema
{
  "statistics": [
    {}
  ],
  "legs": [
    {
      "currentPriceOffset": 50000,
      "quantity": 100,
      "percentageOTM": 0,
      "strikeRelativeLeg": 0,
      "daysUntilExpiration": 0,
      "side": "call",
      "premium": 50000,
      "delta": 100,
      "strikeSelection": "delta",
      "type": "equity",
      "direction": "long"
    }
  ],
  "endDate": "2026-08-16",
  "entryConditions": {
    "frequency": "every day",
    "specificDays": [
      0
    ],
    "maximumActiveTrials": 0,
    "maximumActiveTrialsBehavior": "don't enter",
    "minimumVIX": 0,
    "maximumVIX": 0
  },
  "progress": 0,
  "snapshots": [
    {
      "dateTime": "string",
      "profitLoss": 0,
      "underlyingPrice": 0
    }
  ],
  "startDate": "2026-08-16",
  "ETA": 0,
  "status": "pending",
  "exitConditions": {
    "takeProfitPercentage": 0,
    "stopLossPercentage": 0,
    "afterDaysInTrade": 0,
    "atDaysToExpiration": 0,
    "minimumVIX": 0
  },
  "symbol": "string",
  "notices": [
    "string"
  ],
  "id": "string",
  "trials": [
    {
      "openDateTime": "string",
      "closeDateTime": "string",
      "profitLoss": 0
    }
  ]
}
No links
400	
Request included malformed input

No links
404	
Backtest not found

No links
500	
Failed to process request

No links

GET
/backtests/{id}
Get backtest
Get backtest by ID

Parameters
Name	Description
id *
string
(path)
id
Responses
Code	Description	Links
200	
Backtest details

Media type

application/json
Example Value
Schema
{
  "statistics": [
    {}
  ],
  "legs": [
    {
      "currentPriceOffset": 50000,
      "quantity": 100,
      "percentageOTM": 0,
      "strikeRelativeLeg": 0,
      "daysUntilExpiration": 0,
      "side": "call",
      "premium": 50000,
      "delta": 100,
      "strikeSelection": "delta",
      "type": "equity",
      "direction": "long"
    }
  ],
  "endDate": "2026-08-16",
  "entryConditions": {
    "frequency": "every day",
    "specificDays": [
      0
    ],
    "maximumActiveTrials": 0,
    "maximumActiveTrialsBehavior": "don't enter",
    "minimumVIX": 0,
    "maximumVIX": 0
  },
  "progress": 0,
  "snapshots": [
    {
      "dateTime": "string",
      "profitLoss": 0,
      "underlyingPrice": 0
    }
  ],
  "startDate": "2026-08-16",
  "ETA": 0,
  "status": "pending",
  "exitConditions": {
    "takeProfitPercentage": 0,
    "stopLossPercentage": 0,
    "afterDaysInTrade": 0,
    "atDaysToExpiration": 0,
    "minimumVIX": 0
  },
  "symbol": "string",
  "notices": [
    "string"
  ],
  "id": "string",
  "trials": [
    {
      "openDateTime": "string",
      "closeDateTime": "string",
      "profitLoss": 0
    }
  ]
}
No links
400	
Request included malformed input

No links
404	
Backtest not found

No links
500	
Failed to process request

No links

GET
/backtests/{id}/logs
Get backtest Logs
Read logs of specified backtest

Parameters
Name	Description
id *
string
(path)
id
Responses
Code	Description	Links
200	
Backtest logs

No links
400	
Request included malformed input

No links
404	
Backtest not found

No links

POST
/backtests/{id}/cancel
Cancel backtest
Cancel running backtest

Parameters
Name	Description
id *
string
(path)
id
Responses
Code	Description	Links
204	
Backtest cancelled

No links
400	
Request included malformed input

No links
404	
Backtest not found

No links

GET
/available-dates
Available historical date ranges
Available historical date ranges for every individual symbol

Parameters
No parameters

Responses
Code	Description	Links
200	
List of available symbols and dates

Media type

application/json
Example Value
Schema
[
  {
    "symbol": "string",
    "startDate": "string",
    "endDate": "string"
  }
]
No links
400	
Request included malformed input

No links
500	
Failed to process request

No links

POST
/simulate-trade
Simulate a trade
Returns historical prices for a trade

Parameters
No parameters

Request body

application/json
Examples: 
Covered call
Example Value
Schema
{
  "underlying": "SPY",
  "legs": [
    {
      "symbol": "SPY   190227C00275000",
      "direction": "short",
      "quantity": 1
    },
    {
      "symbol": "SPY",
      "direction": "long",
      "quantity": 100
    }
  ]
}
Responses
Code	Description	Links
200	
Trade simulation successful

Media type

application/json
Example Value
Schema
[
  {
    "dateTime": "2026-08-16T07:07:02.747Z",
    "price": "string",
    "effect": "debit",
    "underlyingPrice": "string",
    "delta": "string"
  }
]
No links

Schemas
BacktestPost{
symbol	string
startDate	string($date)
endDate	string($date)
legs	[...]
entryConditions	{...}
exitConditions	{...}
}
BacktestGet{
id	string
symbol	string
startDate	string($date)
endDate	string($date)
legs	[...]
entryConditions	{...}
exitConditions	{...}
ETA	number
progress	number
status	string
Enum:
Array [ 3 ]
statistics	[...]
trials	[...]
snapshots	[...]
notices	[...]
}
Snapshot{
dateTime	string
profitLoss	number
underlyingPrice	number
}
Trial{
openDateTime	string
closeDateTime	string
profitLoss	number
}
Leg{
type*	string
The instrument type

Enum:
Array [ 2 ]
direction*	string
The position direction

Enum:
Array [ 2 ]
side	string
The option side (only applicable for equity-option type)

Enum:
Array [ 2 ]
quantity*	integer
minimum: 1
maximum: 100
Number of contracts or shares (1-10 for options, 1-100 for stocks)

strikeSelection*	string
How to select the strike for this leg

Enum:
Array [ 7 ]
strikeRelativeLeg	integer
Index of the leg to use as reference when strike selection is relative to another leg

delta	number
minimum: 1
maximum: 100
Delta value when strikeSelection is "delta" (expressed as integer from 1 to 100)

percentageOTM	number
Percentage OTM when strikeSelection is "percentageOTM" or "percentageOTMRelative" (e.g. 0.1 for 10% OTM, -0.1 for 10% ITM)

currentPriceOffset	number
maximum: 50000
Offset from current price when strikeSelection is currentPriceOffset related (max 50000)

premium	number
maximum: 50000
Premium value when strikeSelection is "premium" (max 50000)

daysUntilExpiration*	integer
Days until expiration for this leg

}
EntryConditions{
frequency	string
nullable: true
Enum:
Array [ 3 ]
specificDays	[...]
maximumActiveTrials	integer
nullable: true
maximumActiveTrialsBehavior	string
nullable: true
Enum:
Array [ 2 ]
minimumVIX	integer
nullable: true
maximumVIX	integer
nullable: true
}
ExitConditions{
takeProfitPercentage	integer
nullable: true
stopLossPercentage	integer
nullable: true
afterDaysInTrade	integer
nullable: true
atDaysToExpiration	integer
nullable: true
minimumVIX	integer
nullable: true
}
AvailableSymbolDates{
symbol	string
startDate	string
endDate	string
}

Account Positions
 0.0.1 
[ Base URL: api.tastyworks.com ]

Api Version 20240501
Download Open-api Json
For this API version, you'll need to include a 'Accept-Version' header with a value of '20240501'. If you leave out that header, you will hit the default API version (20240430).
Filter by tag
balance-snapshots
Operations about balance-snapshots



GET
/accounts/{account_number}/balance-snapshots
Returns most recent snapshot and current balance for an account

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 250

250
currency
string
(query)
Currency

Default value : USD

USD
snapshot-date
string($date)
(query)
The day of the balance snapshot to retrieve

snapshot-date
time-of-day *
string
(query)
The abbreviation for the time of day.

Available values : BOD, EOD, Intraday


BOD
start-date
string($date)
(query)
The first date in a range of dates to retrieve

start-date
end-date
string($date)
(query)
The last date in a range of dates to retrieve

end-date
Responses
Response content type

application/json
Code	Description
200	
Returns most recent snapshot and current balance for an account

Example Value
Model
[
  {
    "futures-margin-requirement": 0,
    "unsettled-cryptocurrency-fiat-amount": 0,
    "cash-balance": 0,
    "intraday-futures-cash-effective-date": "2026-08-16",
    "equity-offering-margin-requirement": 0,
    "short-futures-value": 0,
    "total-settle-balance": 0,
    "long-futures-value": 0,
    "long-margineable-value": 0,
    "margin-equity": 0,
    "short-margineable-value": 0,
    "fixed-income-security-margin-requirement": 0,
    "intraday-futures-cash-amount": 0,
    "time-of-day": "string",
    "short-cryptocurrency-value": 0,
    "net-liquidating-value": 0,
    "long-cryptocurrency-value": 0,
    "maintenance-requirement": 0,
    "pending-cash": 0,
    "account-number": "string",
    "used-derivative-buying-power": 0,
    "sma-equity-option-buying-power": 0,
    "bond-margin-requirement": 0,
    "special-memorandum-account-value": 0,
    "day-trade-excess": 0,
    "closed-loop-available-balance": 0,
    "intraday-equities-cash-effect": "string",
    "currency": "string",
    "day-trading-buying-power": 0,
    "intraday-equities-cash-effective-date": "2026-08-16",
    "reg-t-call-value": 0,
    "cash-settle-balance": 0,
    "previous-day-cryptocurrency-fiat-effect": "string",
    "pending-cash-effect": "string",
    "special-memorandum-account-apex-adjustment": 0,
    "intraday-equities-cash-amount": 0,
    "snapshot-date": "2026-08-16",
    "long-bond-value": 0,
    "long-futures-derivative-value": 0,
    "day-equity-call-value": 0,
    "previous-date-cryptocurrency-fiat-effective-date": "2026-08-16",
    "margin-settle-balance": 0,
    "short-futures-derivative-value": 0,
    "long-equity-value": 0,
    "short-equity-value": 0,
    "derivative-buying-power": 0,
    "unsettled-cryptocurrency-fiat-effect": "string",
    "previous-day-cryptocurrency-fiat-amount": 0,
    "maintenance-call-value": 0,
    "long-derivative-value": 0,
    "equity-buying-power": 0,
    "short-derivative-value": 0,
    "cash-available-to-withdraw": 0,
    "available-trading-funds": 0,
    "long-fixed-income-security-value": 0,
    "cryptocurrency-margin-requirement": 0,
    "intraday-futures-cash-effect": "string",
    "day-trading-call-value": 0
  }
]
accounts
Operations about accounts



GET
/accounts/{account_number}/balances
Returns the current balances for an account

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
Responses
Response content type

application/json
Code	Description
200	
Returns the current balances for an account

Example Value
Model
[
  {
    "futures-margin-requirement": 0,
    "unsettled-cryptocurrency-fiat-amount": 0,
    "cash-balance": 0,
    "intraday-futures-cash-effective-date": "2026-08-16",
    "equity-offering-margin-requirement": 0,
    "short-futures-value": 0,
    "total-settle-balance": 0,
    "long-futures-value": 0,
    "long-margineable-value": 0,
    "margin-equity": 0,
    "short-margineable-value": 0,
    "updated-at": "2026-08-16T07:07:40.327Z",
    "fixed-income-security-margin-requirement": 0,
    "intraday-futures-cash-amount": 0,
    "buying-power-adjustment": 0,
    "time-of-day": "string",
    "short-cryptocurrency-value": 0,
    "net-liquidating-value": 0,
    "long-cryptocurrency-value": 0,
    "maintenance-requirement": 0,
    "pending-cash": 0,
    "effective-cryptocurrency-buying-power": 0,
    "account-number": "string",
    "used-derivative-buying-power": 0,
    "futures-overnight-margin-requirement": 0,
    "sma-equity-option-buying-power": 0,
    "bond-margin-requirement": 0,
    "special-memorandum-account-value": 0,
    "day-trade-excess": 0,
    "maintenance-excess": 0,
    "closed-loop-available-balance": 0,
    "intraday-equities-cash-effect": "string",
    "currency": "string",
    "day-trading-buying-power": 0,
    "apex-starting-day-margin-equity": 0,
    "pending-margin-interest": 0,
    "intraday-equities-cash-effective-date": "2026-08-16",
    "reg-t-call-value": 0,
    "cash-settle-balance": 0,
    "previous-day-cryptocurrency-fiat-effect": "string",
    "pending-cash-effect": "string",
    "reg-t-margin-requirement": 0,
    "special-memorandum-account-apex-adjustment": 0,
    "futures-intraday-margin-requirement": 0,
    "intraday-equities-cash-amount": 0,
    "short-index-derivative-value": 0,
    "snapshot-date": "2026-08-16",
    "long-index-derivative-value": 0,
    "long-bond-value": 0,
    "buying-power-adjustment-effect": "string",
    "long-futures-derivative-value": 0,
    "day-equity-call-value": 0,
    "previous-date-cryptocurrency-fiat-effective-date": "2026-08-16",
    "margin-settle-balance": 0,
    "short-futures-derivative-value": 0,
    "long-equity-value": 0,
    "short-equity-value": 0,
    "derivative-buying-power": 0,
    "total-pending-liquidity-pool-rebate": 0,
    "unsettled-cryptocurrency-fiat-effect": "string",
    "previous-day-cryptocurrency-fiat-amount": 0,
    "maintenance-call-value": 0,
    "long-derivative-value": 0,
    "equity-buying-power": 0,
    "short-derivative-value": 0,
    "cash-available-to-withdraw": 0,
    "available-trading-funds": 0,
    "long-fixed-income-security-value": 0,
    "cryptocurrency-margin-requirement": 0,
    "intraday-futures-cash-effect": "string",
    "day-trading-call-value": 0
  }
]

GET
/accounts/{account_number}/balances/{currency}
Returns the current balance values for an account and currency

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
currency *
string
(path)
currency
Responses
Response content type

application/json
Code	Description
200	
Returns the current balance values for an account and currency

Example Value
Model
{
  "futures-margin-requirement": 0,
  "unsettled-cryptocurrency-fiat-amount": 0,
  "cash-balance": 0,
  "intraday-futures-cash-effective-date": "2026-08-16",
  "equity-offering-margin-requirement": 0,
  "short-futures-value": 0,
  "total-settle-balance": 0,
  "long-futures-value": 0,
  "long-margineable-value": 0,
  "margin-equity": 0,
  "short-margineable-value": 0,
  "updated-at": "2026-08-16T07:07:40.334Z",
  "fixed-income-security-margin-requirement": 0,
  "intraday-futures-cash-amount": 0,
  "buying-power-adjustment": 0,
  "time-of-day": "string",
  "short-cryptocurrency-value": 0,
  "net-liquidating-value": 0,
  "long-cryptocurrency-value": 0,
  "maintenance-requirement": 0,
  "pending-cash": 0,
  "effective-cryptocurrency-buying-power": 0,
  "account-number": "string",
  "used-derivative-buying-power": 0,
  "futures-overnight-margin-requirement": 0,
  "sma-equity-option-buying-power": 0,
  "bond-margin-requirement": 0,
  "special-memorandum-account-value": 0,
  "day-trade-excess": 0,
  "maintenance-excess": 0,
  "closed-loop-available-balance": 0,
  "intraday-equities-cash-effect": "string",
  "currency": "string",
  "day-trading-buying-power": 0,
  "apex-starting-day-margin-equity": 0,
  "pending-margin-interest": 0,
  "intraday-equities-cash-effective-date": "2026-08-16",
  "reg-t-call-value": 0,
  "cash-settle-balance": 0,
  "previous-day-cryptocurrency-fiat-effect": "string",
  "pending-cash-effect": "string",
  "reg-t-margin-requirement": 0,
  "special-memorandum-account-apex-adjustment": 0,
  "futures-intraday-margin-requirement": 0,
  "intraday-equities-cash-amount": 0,
  "short-index-derivative-value": 0,
  "snapshot-date": "2026-08-16",
  "long-index-derivative-value": 0,
  "long-bond-value": 0,
  "buying-power-adjustment-effect": "string",
  "long-futures-derivative-value": 0,
  "day-equity-call-value": 0,
  "previous-date-cryptocurrency-fiat-effective-date": "2026-08-16",
  "margin-settle-balance": 0,
  "short-futures-derivative-value": 0,
  "long-equity-value": 0,
  "short-equity-value": 0,
  "derivative-buying-power": 0,
  "total-pending-liquidity-pool-rebate": 0,
  "unsettled-cryptocurrency-fiat-effect": "string",
  "previous-day-cryptocurrency-fiat-amount": 0,
  "maintenance-call-value": 0,
  "long-derivative-value": 0,
  "equity-buying-power": 0,
  "short-derivative-value": 0,
  "cash-available-to-withdraw": 0,
  "available-trading-funds": 0,
  "long-fixed-income-security-value": 0,
  "cryptocurrency-margin-requirement": 0,
  "intraday-futures-cash-effect": "string",
  "day-trading-call-value": 0
}
positions
Operations about positions



GET
/accounts/{account_number}/positions
Returns a list of the account's positions.
Can be filtered by symbol, underlying_symbol

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
include-closed-positions
boolean
(query)
If closed positions should be included in the query

Default value : false


false
include-marks
boolean
(query)
Include current quote mark (note: can decrease performance)

Default value : false


false
instrument-type
string
(query)
The type of Instrument

Available values : Bond, Cryptocurrency, Currency Pair, Equity, Equity Offering, Equity Option, Event Contract, Fixed Income Security, Future, Future Option, Index, Liquidity Pool, Unknown, Warrant


--
net-positions
boolean
(query)
Returns net positions grouped by instrument type and symbol

Default value : false


false
symbol
string
(query)
A single symbol. Stock Ticker Symbol AAPL,
OCC Option Symbol AAPL 191004P00275000,
TW Future Symbol /ESZ9, or
TW Future Option Symbol ./ESZ9 EW4U9 190927P2975

symbol
underlying-product-code
string
(query)
The underlying Future's Product code. i.e ES

underlying-product-code
underlying-symbol
array[string]
(query)
An array of Underlying symbol(s) for positions (example: underlying-symbol[]={value1}&underlying-symbol[]={value2})

partition-keys
array[string]
(formData)
Account partition keys (example: partition-keys[]={value1}&partition-keys[]={value2})

Responses
Response content type

application/json
Code	Description
200	
Returns a list of the account's positions.
Can be filtered by symbol, underlying_symbol

Example Value
Model
[
  {
    "instrument-type": "string",
    "multiplier": 0,
    "face-value": 0,
    "realized-today": 0,
    "is-frozen": true,
    "updated-at": "2026-08-16T07:07:40.344Z",
    "average-daily-market-close-price": 0,
    "deliverable-type": "string",
    "underlying-symbol": "string",
    "mark-price": 0,
    "account-number": "string",
    "fixing-price": 0,
    "quantity": {},
    "realized-day-gain-date": "2026-08-16",
    "expires-at": "2026-08-16T07:07:40.344Z",
    "par-size": 0,
    "mark": 0,
    "realized-day-gain": 0,
    "realized-day-gain-effect": "string",
    "cost-effect": "string",
    "close-price": 0,
    "average-yearly-market-close-price": 0,
    "average-open-price": 0,
    "is-suppressed": true,
    "created-at": "2026-08-16T07:07:40.344Z",
    "symbol": "string",
    "streamer-symbol": "string",
    "realized-today-date": "2026-08-16",
    "order-id": 0,
    "realized-today-effect": "string",
    "quantity-direction": "string",
    "restricted-quantity": {},
    "update-type": "string"
  }
]
Positions
Allows an API client to fetch data about account positions.



Models
CurrentPosition{
description:	
CurrentPosition model

account-number	string
instrument-type	string
streamer-symbol	string
symbol	string
underlying-symbol	string
quantity	{...}
average-daily-market-close-price	number($double)
average-open-price	number($double)
average-yearly-market-close-price	number($double)
close-price	number($double)
cost-effect	string
is-frozen	boolean
is-suppressed	boolean
mark	number($double)
mark-price	number($double)
multiplier	number($double)
quantity-direction	string
restricted-quantity	{...}
expires-at	string($date-time)
deliverable-type	string
fixing-price	number($double)
realized-day-gain	number($double)
realized-day-gain-date	string($date)
realized-day-gain-effect	string
realized-today	number($double)
realized-today-date	string($date)
realized-today-effect	string
face-value	number($double)
par-size	number($double)
created-at	string($date-time)
updated-at	string($date-time)
order-id	integer($int32)
update-type	string
}
AccountBalance{
description:	
AccountBalance model

account-number	string
available-trading-funds	number($double)
bond-margin-requirement	number($double)
cash-available-to-withdraw	number($double)
cash-balance	number($double)
cash-settle-balance	number($double)
closed-loop-available-balance	number($double)
cryptocurrency-margin-requirement	number($double)
currency	string
day-equity-call-value	number($double)
day-trade-excess	number($double)
day-trading-buying-power	number($double)
day-trading-call-value	number($double)
derivative-buying-power	number($double)
equity-buying-power	number($double)
equity-offering-margin-requirement	number($double)
fixed-income-security-margin-requirement	number($double)
futures-margin-requirement	number($double)
intraday-equities-cash-amount	number($double)
intraday-equities-cash-effect	string
intraday-equities-cash-effective-date	string($date)
intraday-futures-cash-amount	number($double)
intraday-futures-cash-effect	string
intraday-futures-cash-effective-date	string($date)
long-bond-value	number($double)
long-cryptocurrency-value	number($double)
long-derivative-value	number($double)
long-equity-value	number($double)
long-fixed-income-security-value	number($double)
long-futures-derivative-value	number($double)
long-futures-value	number($double)
long-margineable-value	number($double)
maintenance-call-value	number($double)
maintenance-requirement	number($double)
margin-equity	number($double)
margin-settle-balance	number($double)
net-liquidating-value	number($double)
pending-cash	number($double)
pending-cash-effect	string
previous-date-cryptocurrency-fiat-effective-date	string($date)
previous-day-cryptocurrency-fiat-amount	number($double)
previous-day-cryptocurrency-fiat-effect	string
reg-t-call-value	number($double)
short-cryptocurrency-value	number($double)
short-derivative-value	number($double)
short-equity-value	number($double)
short-futures-derivative-value	number($double)
short-futures-value	number($double)
short-margineable-value	number($double)
sma-equity-option-buying-power	number($double)
special-memorandum-account-apex-adjustment	number($double)
special-memorandum-account-value	number($double)
total-settle-balance	number($double)
unsettled-cryptocurrency-fiat-amount	number($double)
unsettled-cryptocurrency-fiat-effect	string
used-derivative-buying-power	number($double)
snapshot-date	string($date)
time-of-day	string
reg-t-margin-requirement	number($double)
futures-overnight-margin-requirement	number($double)
futures-intraday-margin-requirement	number($double)
maintenance-excess	number($double)
pending-margin-interest	number($double)
apex-starting-day-margin-equity	number($double)
buying-power-adjustment	number($double)
buying-power-adjustment-effect	string
effective-cryptocurrency-buying-power	number($double)
total-pending-liquidity-pool-rebate	number($double)
long-index-derivative-value	number($double)
short-index-derivative-value	number($double)
updated-at	string($date-time)
}
AccountBalanceSnapshot{
description:	
AccountBalanceSnapshot model

account-number	string
available-trading-funds	number($double)
bond-margin-requirement	number($double)
cash-available-to-withdraw	number($double)
cash-balance	number($double)
cash-settle-balance	number($double)
closed-loop-available-balance	number($double)
cryptocurrency-margin-requirement	number($double)
currency	string
day-equity-call-value	number($double)
day-trade-excess	number($double)
day-trading-buying-power	number($double)
day-trading-call-value	number($double)
derivative-buying-power	number($double)
equity-buying-power	number($double)
equity-offering-margin-requirement	number($double)
fixed-income-security-margin-requirement	number($double)
futures-margin-requirement	number($double)
intraday-equities-cash-amount	number($double)
intraday-equities-cash-effect	string
intraday-equities-cash-effective-date	string($date)
intraday-futures-cash-amount	number($double)
intraday-futures-cash-effect	string
intraday-futures-cash-effective-date	string($date)
long-bond-value	number($double)
long-cryptocurrency-value	number($double)
long-derivative-value	number($double)
long-equity-value	number($double)
long-fixed-income-security-value	number($double)
long-futures-derivative-value	number($double)
long-futures-value	number($double)
long-margineable-value	number($double)
maintenance-call-value	number($double)
maintenance-requirement	number($double)
margin-equity	number($double)
margin-settle-balance	number($double)
net-liquidating-value	number($double)
pending-cash	number($double)
pending-cash-effect	string
previous-date-cryptocurrency-fiat-effective-date	string($date)
previous-day-cryptocurrency-fiat-amount	number($double)
previous-day-cryptocurrency-fiat-effect	string
reg-t-call-value	number($double)
short-cryptocurrency-value	number($double)
short-derivative-value	number($double)
short-equity-value	number($double)
short-futures-derivative-value	number($double)
short-futures-value	number($double)
short-margineable-value	number($double)
sma-equity-option-buying-power	number($double)
special-memorandum-account-apex-adjustment	number($double)
special-memorandum-account-value	number($double)
total-settle-balance	number($double)
unsettled-cryptocurrency-fiat-amount	number($double)
unsettled-cryptocurrency-fiat-effect	string
used-derivative-buying-power	number($double)
snapshot-date	string($date)
time-of-day	string
}

Instruments
 0.0.1 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API version is the default version, meaning all requests that exclude the 'Accept-Version' header will be routed here. You can also include the 'Accept-Version' header with a value of '20250715' to explicitly hit this API version. Please note that deprecated API versions will soon reach end of life, at which point all requests for that version will be rejected.
Filter by tag
futures-option-chains
Operations about futures-option-chains



GET
/futures-option-chains/{symbol}
Returns a futures option chain given a futures product code, i.e. ES

Parameters
Name	Description
symbol *
integer($int32)
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a futures option chain given a futures product code, i.e. ES

Example Value
Model
[
  {
    "future-option-product": {
      "root-symbol": "string",
      "exchange": "string",
      "settlement-delay-days": 0,
      "itm-rule": "string",
      "code": "string",
      "supported": true,
      "market-sector": "string",
      "product-type": "string",
      "expiration-type": "string",
      "display-factor": 0,
      "is-am-settled": true,
      "product-subtype": "string",
      "cash-settled": true
    },
    "multiplier": 0,
    "root-symbol": "string",
    "exchange": "string",
    "notional-value": 0,
    "active": true,
    "is-closing-only": true,
    "underlying-symbol": "string",
    "security-id": "string",
    "maturity-date": "2026-08-16",
    "is-exercisable-weekly": true,
    "strike-factor": 0,
    "product-code": "string",
    "days-to-expiration": 0,
    "option-root-symbol": "string",
    "expiration-date": "2026-08-16",
    "expires-at": "2026-08-16T07:08:56.431Z",
    "last-trade-time": "string",
    "strike-price": 0,
    "is-primary-deliverable": true,
    "option-type": "string",
    "symbol": "string",
    "is-vanilla": true,
    "streamer-symbol": "string",
    "display-factor": 0,
    "stops-trading-at": "2026-08-16T07:08:56.431Z",
    "exercise-style": "string",
    "is-confirmed": true,
    "future-price-ratio": 0,
    "settlement-type": "string",
    "underlying-count": 0
  }
]

GET
/futures-option-chains/{symbol}/nested
Returns a futures option chain given a futures product code in a nested form to minimize redundant processing

Parameters
Name	Description
symbol *
integer($int32)
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a futures option chain given a futures product code in a nested form to minimize redundant processing

Example Value
Model
{
  "futures": {
    "root-symbol": "string",
    "maturity-date": "2026-08-16",
    "days-to-expiration": 0,
    "expiration-date": "2026-08-16",
    "expires-at": "2026-08-16T07:08:56.433Z",
    "next-active-month": true,
    "symbol": "string",
    "streamer-symbol": "string",
    "active-month": true,
    "stops-trading-at": "2026-08-16T07:08:56.433Z"
  },
  "option-chains": {
    "underlying-symbol": "string",
    "root-symbol": "string",
    "exercise-style": "string",
    "expirations": {
      "strikes": {
        "strike-price": 0,
        "call": "string",
        "call-streamer-symbol": "string",
        "put": "string",
        "put-streamer-symbol": "string"
      },
      "tick-sizes": {
        "symbol": "string",
        "threshold": 0,
        "value": 0
      },
      "root-symbol": "string",
      "notional-value": 0,
      "underlying-symbol": "string",
      "strike-factor": 0,
      "days-to-expiration": 0,
      "option-root-symbol": "string",
      "expiration-date": "2026-08-16",
      "expires-at": "2026-08-16T07:08:56.433Z",
      "asset": "string",
      "expiration-type": "string",
      "display-factor": 0,
      "option-contract-symbol": "string",
      "stops-trading-at": "2026-08-16T07:08:56.433Z",
      "settlement-type": "string",
      "cash-settled": true
    }
  }
}
instruments
Operations about instruments



GET
/instruments/cryptocurrencies
Retrieve a set of cryptocurrencies given an array of one or more symbols.

Parameters
Name	Description
symbol
array[string]
(query)
Symbol(s) (example: symbol[]={value1}&symbol[]={value2})

Responses
Response content type

application/json
Code	Description
200	
Retrieve a set of cryptocurrencies given an array of one or more symbols.

Example Value
Model
[
  {
    "instrument-type": "string",
    "active": true,
    "is-closing-only": true,
    "short-description": "string",
    "tick-size": 0,
    "symbol": "string",
    "streamer-symbol": "string",
    "id": 0,
    "description": "string"
  }
]

GET
/instruments/cryptocurrencies/{symbol}
Retrieve a cryptocurrency given a symbol.

Parameters
Name	Description
symbol *
string
(path)
Symbol

symbol
Responses
Response content type

application/json
Code	Description
200	
Retrieve a cryptocurrency given a symbol.

Example Value
Model
{
  "instrument-type": "string",
  "active": true,
  "is-closing-only": true,
  "short-description": "string",
  "tick-size": 0,
  "symbol": "string",
  "streamer-symbol": "string",
  "id": 0,
  "description": "string"
}

GET
/instruments/equities
Returns a set of equity definitions given an array of one or more symbols

Parameters
Name	Description
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
is-etf
boolean
(query)
Flag indicating if equity is an etf instrument


--
is-index
boolean
(query)
Flag indicating if equity is an index instrument


--
lendability
string
(query)
Lendability

Available values : Easy To Borrow, Locate Required, Preborrow


--
symbol
array[string]
(query)
The symbol of the equity(s), i.e AAPL (example: symbol[]={value1}&symbol[]={value2})

Responses
Response content type

application/json
Code	Description
200	
Returns a set of equity definitions given an array of one or more symbols

Example Value
Model
[
  {
    "halted-at": "2026-08-16T07:08:56.442Z",
    "instrument-type": "string",
    "tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "is-illiquid": true,
    "overnight-trading-permitted": true,
    "country-of-incorporation": "string",
    "active": true,
    "is-closing-only": true,
    "underlying-product-type": "string",
    "country-of-taxation": "string",
    "short-description": "string",
    "listed-market": "string",
    "instrument-sub-type": "string",
    "is-index": true,
    "is-etf": true,
    "market-time-instrument-collection": "string",
    "is-options-closing-only": true,
    "symbol": "string",
    "borrow-rate": 0,
    "streamer-symbol": "string",
    "option-tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "bypass-manual-review": true,
    "lendability": "string",
    "stops-trading-at": "2026-08-16T07:08:56.442Z",
    "is-fractional-quantity-eligible": true,
    "description": "string",
    "pre-ipo": true
  }
]

GET
/instruments/equities/active
Returns all active equities in a paginated fashion

Parameters
Name	Description
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
lendability
string
(query)
Lendability. i.e. Easy To Borrow, Locate Required or Preborrow

Available values : Easy To Borrow, Locate Required, Preborrow


--
Responses
Response content type

application/json
Code	Description
200	
Returns all active equities in a paginated fashion

Example Value
Model
[
  {
    "halted-at": "2026-08-16T07:08:56.447Z",
    "instrument-type": "string",
    "tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "is-illiquid": true,
    "overnight-trading-permitted": true,
    "country-of-incorporation": "string",
    "active": true,
    "is-closing-only": true,
    "underlying-product-type": "string",
    "country-of-taxation": "string",
    "short-description": "string",
    "listed-market": "string",
    "instrument-sub-type": "string",
    "is-index": true,
    "is-etf": true,
    "market-time-instrument-collection": "string",
    "is-options-closing-only": true,
    "symbol": "string",
    "borrow-rate": 0,
    "streamer-symbol": "string",
    "option-tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "bypass-manual-review": true,
    "lendability": "string",
    "stops-trading-at": "2026-08-16T07:08:56.447Z",
    "is-fractional-quantity-eligible": true,
    "description": "string",
    "pre-ipo": true
  }
]

GET
/instruments/equities/{symbol}
Returns a single equity definition for the provided symbol

Parameters
Name	Description
symbol *
string
(path)
The symbol of the equity, i.e. AAPL

symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a single equity definition for the provided symbol

Example Value
Model
{
  "halted-at": "2026-08-16T07:08:56.452Z",
  "instrument-type": "string",
  "tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "is-illiquid": true,
  "overnight-trading-permitted": true,
  "country-of-incorporation": "string",
  "active": true,
  "is-closing-only": true,
  "underlying-product-type": "string",
  "country-of-taxation": "string",
  "short-description": "string",
  "listed-market": "string",
  "instrument-sub-type": "string",
  "is-index": true,
  "is-etf": true,
  "market-time-instrument-collection": "string",
  "is-options-closing-only": true,
  "symbol": "string",
  "borrow-rate": 0,
  "streamer-symbol": "string",
  "option-tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "bypass-manual-review": true,
  "lendability": "string",
  "stops-trading-at": "2026-08-16T07:08:56.452Z",
  "is-fractional-quantity-eligible": true,
  "description": "string",
  "pre-ipo": true
}

GET
/instruments/equity-options/{symbol}
Get equity option by symbol

Parameters
Name	Description
symbol *
string
(path)
The symbol of the equity option using OCC Symbology, i.e. FB 180629C00200000

symbol
active
boolean
(query)
Whether an option is available for trading with the broker.
Terminology is somewhat misleading as this is generally used to filter non-standard / flex options out.


--
Responses
Response content type

application/json
Code	Description
200	
Get equity option by symbol

Example Value
Model
{
  "halted-at": "2026-08-16T07:08:56.456Z",
  "instrument-type": "string",
  "root-symbol": "string",
  "active": true,
  "is-closing-only": true,
  "underlying-symbol": "string",
  "days-to-expiration": 0,
  "expiration-date": "2026-08-16",
  "expires-at": "2026-08-16T07:08:56.456Z",
  "listed-market": "string",
  "strike-price": 0,
  "old-security-number": "string",
  "option-type": "string",
  "market-time-instrument-collection": "string",
  "symbol": "string",
  "streamer-symbol": "string",
  "expiration-type": "string",
  "shares-per-contract": 0,
  "stops-trading-at": "2026-08-16T07:08:56.456Z",
  "exercise-style": "string",
  "settlement-type": "string",
  "option-chain-type": "string"
}

GET
/instruments/future-option-products
Returns metadata for all supported future option products

Parameters
Name	Description
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
Responses
Response content type

application/json
Code	Description
200	
Returns metadata for all supported future option products

Example Value
Model
[
  {
    "root-symbol": "string",
    "exchange": "string",
    "settlement-delay-days": 0,
    "itm-rule": "string",
    "code": "string",
    "supported": true,
    "market-sector": "string",
    "product-type": "string",
    "expiration-type": "string",
    "display-factor": 0,
    "is-am-settled": true,
    "product-subtype": "string",
    "cash-settled": true
  }
]

GET
/instruments/future-option-products/{exchange}/{root_symbol}
Get a future option product by exchange and root symbol

Parameters
Name	Description
exchange *
string
(path)
exchange
root_symbol *
string
(path)
root_symbol
Responses
Response content type

application/json
Code	Description
200	
Get a future option product by exchange and root symbol

Example Value
Model
{
  "root-symbol": "string",
  "exchange": "string",
  "settlement-delay-days": 0,
  "itm-rule": "string",
  "code": "string",
  "supported": true,
  "market-sector": "string",
  "product-type": "string",
  "expiration-type": "string",
  "display-factor": 0,
  "is-am-settled": true,
  "product-subtype": "string",
  "cash-settled": true
}

GET
/instruments/future-option-products/{root_symbol}

GET
/instruments/future-options/{symbol}
Returns a future option given a symbol. Uses TW symbology: ./ESZ9 EW4U9 190927P2975

Parameters
Name	Description
symbol *
string
(path)
The symbol of the future option

symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a future option given a symbol. Uses TW symbology: ./ESZ9 EW4U9 190927P2975

Example Value
Model
{
  "future-option-product": {
    "root-symbol": "string",
    "exchange": "string",
    "settlement-delay-days": 0,
    "itm-rule": "string",
    "code": "string",
    "supported": true,
    "market-sector": "string",
    "product-type": "string",
    "expiration-type": "string",
    "display-factor": 0,
    "is-am-settled": true,
    "product-subtype": "string",
    "cash-settled": true
  },
  "multiplier": 0,
  "root-symbol": "string",
  "exchange": "string",
  "notional-value": 0,
  "active": true,
  "is-closing-only": true,
  "underlying-symbol": "string",
  "security-id": "string",
  "maturity-date": "2026-08-16",
  "is-exercisable-weekly": true,
  "strike-factor": 0,
  "product-code": "string",
  "days-to-expiration": 0,
  "option-root-symbol": "string",
  "expiration-date": "2026-08-16",
  "expires-at": "2026-08-16T07:08:56.464Z",
  "last-trade-time": "string",
  "strike-price": 0,
  "is-primary-deliverable": true,
  "option-type": "string",
  "symbol": "string",
  "is-vanilla": true,
  "streamer-symbol": "string",
  "display-factor": 0,
  "stops-trading-at": "2026-08-16T07:08:56.464Z",
  "exercise-style": "string",
  "is-confirmed": true,
  "future-price-ratio": 0,
  "settlement-type": "string",
  "underlying-count": 0
}

GET
/instruments/future-products
Returns metadata for all supported futures products

Parameters
Name	Description
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
Responses
Response content type

application/json
Code	Description
200	
Returns metadata for all supported futures products

Example Value
Model
[
  {
    "contract-limit": 0,
    "listed-months": "string",
    "root-symbol": "string",
    "exchange": "string",
    "small-notional": true,
    "sub-tick": 0,
    "underlying-description": "string",
    "code": "string",
    "supported": true,
    "notional-multiplier": 0,
    "tick-size": 0,
    "security-group": "string",
    "first-notice": true,
    "streamer-exchange-code": "string",
    "market-sector": "string",
    "back-month-first-calendar-symbol": true,
    "product-type": "string",
    "underlying-identifier": "string",
    "base-tick": 0,
    "display-factor": 0,
    "active-months": "string",
    "price-format": "string",
    "product-subtype": "string",
    "description": "string",
    "true-underlying-code": "string",
    "cash-settled": true
  }
]

GET
/instruments/future-products/{exchange}/{code}
Get future product from exchange and product code

Parameters
Name	Description
exchange *
string
(path)
The exchange code

Available values : CBOED, CFE, CME, SMALLS


CBOED
code *
string
(path)
The product code, i.e. ES

code
Responses
Response content type

application/json
Code	Description
200	
Get future product from exchange and product code

Example Value
Model
{
  "contract-limit": 0,
  "listed-months": "string",
  "root-symbol": "string",
  "exchange": "string",
  "small-notional": true,
  "sub-tick": 0,
  "underlying-description": "string",
  "code": "string",
  "supported": true,
  "notional-multiplier": 0,
  "tick-size": 0,
  "security-group": "string",
  "first-notice": true,
  "streamer-exchange-code": "string",
  "market-sector": "string",
  "back-month-first-calendar-symbol": true,
  "product-type": "string",
  "underlying-identifier": "string",
  "base-tick": 0,
  "display-factor": 0,
  "active-months": "string",
  "price-format": "string",
  "product-subtype": "string",
  "description": "string",
  "true-underlying-code": "string",
  "cash-settled": true
}

GET
/instruments/futures
Returns a set of outright futures given an array of one or more symbols.

Parameters
Name	Description
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
exchange
string
(query)
Name of the exchange for the product. Only used to avoid security id collisions if that is also given.

exchange
only-active-futures
boolean
(query)
If true (defaults to true), only active futures are returned.


--
product-code
array[string]
(query)
The product code of the future(s), i.e. product-code[]=ES&product-code[]=6A.
Ignored if symbol parameter is given. (example: product-code[]={value1}&product-code[]={value2})

security-id
array[string]
(query)
Exchange-specific ID used for order routing (example: security-id[]={value1}&security-id[]={value2})

symbol
array[string]
(query)
The symbol of the future(s), i.e. symbol[]=ESZ9. Leading forward slash is not required. (example: symbol[]={value1}&symbol[]={value2})

Responses
Response content type

application/json
Code	Description
200	
Returns a set of outright futures given an array of one or more symbols.

Example Value
Model
[
  {
    "product-group": "string",
    "tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "exchange": "string",
    "roll-target-symbol": "string",
    "future-product": {
      "contract-limit": 0,
      "listed-months": "string",
      "root-symbol": "string",
      "exchange": "string",
      "small-notional": true,
      "sub-tick": 0,
      "underlying-description": "string",
      "code": "string",
      "supported": true,
      "notional-multiplier": 0,
      "tick-size": 0,
      "security-group": "string",
      "first-notice": true,
      "streamer-exchange-code": "string",
      "market-sector": "string",
      "back-month-first-calendar-symbol": true,
      "product-type": "string",
      "underlying-identifier": "string",
      "base-tick": 0,
      "display-factor": 0,
      "active-months": "string",
      "price-format": "string",
      "product-subtype": "string",
      "description": "string",
      "true-underlying-code": "string",
      "cash-settled": true
    },
    "first-notice-date": "2026-08-16",
    "active": true,
    "is-closing-only": true,
    "security-id": "string",
    "true-underlying-symbol": "string",
    "future-etf-equivalent": {
      "share-quantity": 0,
      "symbol": "string"
    },
    "product-code": "string",
    "expiration-date": "2026-08-16",
    "expires-at": "2026-08-16T07:08:56.476Z",
    "last-trade-date": "2026-08-16",
    "notional-multiplier": 0,
    "sub-fraction": 0,
    "tick-size": 0,
    "next-active-month": true,
    "exchange-data": {},
    "main-fraction": 0,
    "streamer-exchange-code": "string",
    "closing-only-date": "2026-08-16",
    "back-month-first-calendar-symbol": true,
    "symbol": "string",
    "streamer-symbol": "string",
    "active-month": true,
    "option-tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "display-factor": 0,
    "stops-trading-at": "2026-08-16T07:08:56.476Z",
    "is-tradeable": true,
    "spread-tick-sizes": {
      "symbol": "string",
      "threshold": 0,
      "value": 0
    },
    "contract-size": 0
  }
]

GET
/instruments/futures/{symbol}
Returns an outright future given a symbol.

Parameters
Name	Description
symbol *
string
(path)
The symbol of the future, i.e. ESZ9. Leading forward slash is not required.

symbol
Responses
Response content type

application/json
Code	Description
200	
Returns an outright future given a symbol.

Example Value
Model
{
  "product-group": "string",
  "tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "exchange": "string",
  "roll-target-symbol": "string",
  "future-product": {
    "contract-limit": 0,
    "listed-months": "string",
    "root-symbol": "string",
    "exchange": "string",
    "small-notional": true,
    "sub-tick": 0,
    "underlying-description": "string",
    "code": "string",
    "supported": true,
    "notional-multiplier": 0,
    "tick-size": 0,
    "security-group": "string",
    "first-notice": true,
    "streamer-exchange-code": "string",
    "market-sector": "string",
    "back-month-first-calendar-symbol": true,
    "product-type": "string",
    "underlying-identifier": "string",
    "base-tick": 0,
    "display-factor": 0,
    "active-months": "string",
    "price-format": "string",
    "product-subtype": "string",
    "description": "string",
    "true-underlying-code": "string",
    "cash-settled": true
  },
  "first-notice-date": "2026-08-16",
  "active": true,
  "is-closing-only": true,
  "security-id": "string",
  "true-underlying-symbol": "string",
  "future-etf-equivalent": {
    "share-quantity": 0,
    "symbol": "string"
  },
  "product-code": "string",
  "expiration-date": "2026-08-16",
  "expires-at": "2026-08-16T07:08:56.480Z",
  "last-trade-date": "2026-08-16",
  "notional-multiplier": 0,
  "sub-fraction": 0,
  "tick-size": 0,
  "next-active-month": true,
  "exchange-data": {},
  "main-fraction": 0,
  "streamer-exchange-code": "string",
  "closing-only-date": "2026-08-16",
  "back-month-first-calendar-symbol": true,
  "symbol": "string",
  "streamer-symbol": "string",
  "active-month": true,
  "option-tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "display-factor": 0,
  "stops-trading-at": "2026-08-16T07:08:56.480Z",
  "is-tradeable": true,
  "spread-tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "contract-size": 0
}

GET
/instruments/quantity-decimal-precisions
Retrieve all quantity decimal precisions.

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Retrieve all quantity decimal precisions.

Example Value
Model
[
  {
    "instrument-type": "string",
    "minimum-increment-precision": 0,
    "symbol": "string",
    "value": 0
  }
]

GET
/instruments/search
Search instruments by symbol, description, or tags

Parameters
Name	Description
category
string
(query)
Comma-separated categories to filter (case-insensitive)

category
exchange
string
(query)
Comma-separated exchanges to filter (case-insensitive)

exchange
from-date
string($date)
(query)
Lower bound on stops_trading_at; surfaces expired events that stopped trading on/after this date

from-date
instrument-sub-type
string
(query)
Comma-separated instrument sub-types to filter (equities only): ETF, Index

instrument-sub-type
limit
integer($int32)
(query)
Maximum number of results

Default value : 25

25
query
string
(query)
Search query

query
type
string
(query)
Comma-separated instrument types to filter

type
Responses
Response content type

application/json
Code	Description
200	
Search instruments by symbol, description, or tags

Example Value
Model
[
  {
    "instrument-type": "string",
    "exchange": "string",
    "underlying-streamer-symbol": "string",
    "underlying-product-type": "string",
    "sub-category": "string",
    "event-product-external-id": "string",
    "expiration-value": "string",
    "symbol": "string",
    "stops-trading-at": "2026-08-16T07:08:56.487Z",
    "description": "string",
    "external-id": "string",
    "category": "string",
    "strike-types": "string",
    "underlying-product": "string"
  }
]

GET
/instruments/warrants
Returns a set of warrant definitions that can be filtered by parameters

Parameters
Name	Description
symbol
array[string]
(query)
The symbol of the warrant(s), i.e NKLAW (example: symbol[]={value1}&symbol[]={value2})

Responses
Response content type

application/json
Code	Description
200	
Returns a set of warrant definitions that can be filtered by parameters

Example Value
Model
[
  {
    "active": true,
    "cusip": "string",
    "description": "string",
    "instrument-type": "string",
    "is-closing-only": true,
    "listed-market": "string",
    "symbol": "string"
  }
]

GET
/instruments/warrants/{symbol}
Returns a single warrant definition for the provided symbol

Parameters
Name	Description
symbol *
string
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a single warrant definition for the provided symbol

Example Value
Model
{
  "active": true,
  "cusip": "string",
  "description": "string",
  "instrument-type": "string",
  "is-closing-only": true,
  "listed-market": "string",
  "symbol": "string"
}
option-chains
Operations about option-chains



GET
/option-chains/{symbol}
Returns an option chain given an underlying symbol, i.e. AAPL

Parameters
Name	Description
symbol *
integer($int32)
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns an option chain given an underlying symbol, i.e. AAPL

Example Value
Model
[
  {
    "halted-at": "2026-08-16T07:08:56.496Z",
    "instrument-type": "string",
    "root-symbol": "string",
    "active": true,
    "is-closing-only": true,
    "underlying-symbol": "string",
    "days-to-expiration": 0,
    "expiration-date": "2026-08-16",
    "expires-at": "2026-08-16T07:08:56.496Z",
    "listed-market": "string",
    "strike-price": 0,
    "old-security-number": "string",
    "option-type": "string",
    "market-time-instrument-collection": "string",
    "symbol": "string",
    "streamer-symbol": "string",
    "expiration-type": "string",
    "shares-per-contract": 0,
    "stops-trading-at": "2026-08-16T07:08:56.496Z",
    "exercise-style": "string",
    "settlement-type": "string",
    "option-chain-type": "string"
  }
]

GET
/option-chains/{symbol}/compact
Returns an option chain given an underlying symbol, i.e. AAPL in a compact form to minimize content size

Parameters
Name	Description
symbol *
integer($int32)
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns an option chain given an underlying symbol, i.e. AAPL in a compact form to minimize content size

Example Value
Model
{
  "root-symbol": "string",
  "underlying-symbol": "string",
  "deliverables": {
    "id": 0,
    "amount": 0,
    "deliverable-type": "string",
    "description": "string",
    "instrument-type": "string",
    "percent": "string",
    "root-symbol": "string",
    "symbol": "string"
  },
  "expiration-type": "string",
  "streamer-symbols": "string",
  "shares-per-contract": 0,
  "symbols": "string",
  "settlement-type": "string",
  "option-chain-type": "string"
}

GET
/option-chains/{symbol}/nested
Returns an option chain given an underlying symbol,
i.e. AAPL in a nested form to minimize redundant processing

Parameters
Name	Description
symbol *
integer($int32)
(path)
symbol
Responses
Response content type

application/json
Code	Description
200	
Returns an option chain given an underlying symbol,
i.e. AAPL in a nested form to minimize redundant processing

Example Value
Model
{
  "underlying-symbol": "string",
  "root-symbol": "string",
  "option-chain-type": "string",
  "shares-per-contract": 0,
  "tick-sizes": {
    "symbol": "string",
    "threshold": 0,
    "value": 0
  },
  "deliverables": {
    "id": 0,
    "amount": 0,
    "deliverable-type": "string",
    "description": "string",
    "instrument-type": "string",
    "percent": "string",
    "root-symbol": "string",
    "symbol": "string"
  },
  "expirations": {
    "expiration-type": "string",
    "expiration-date": "2026-08-16",
    "days-to-expiration": 0,
    "settlement-type": "string",
    "strikes": {
      "strike-price": 0,
      "call": "string",
      "call-streamer-symbol": "string",
      "put": "string",
      "put-streamer-symbol": "string"
    }
  }
}
Instruments
Allows an API client to fetch data about instruments.



Models
FutureOption{
description:	
FutureOption model

active	boolean
days-to-expiration	integer($int32)
display-factor	number($double)
exchange	string
exercise-style	string
expiration-date	string($date)
expires-at	string($date-time)
future-price-ratio	number($double)
is-closing-only	boolean
is-confirmed	boolean
is-exercisable-weekly	boolean
is-primary-deliverable	boolean
is-vanilla	boolean
last-trade-time	string
maturity-date	string($date)
multiplier	number($double)
notional-value	number($double)
option-root-symbol	string
option-type	string
product-code	string
root-symbol	string
security-id	string
settlement-type	string
stops-trading-at	string($date-time)
streamer-symbol	string
strike-factor	number($double)
strike-price	number($double)
symbol	string
underlying-count	number($double)
underlying-symbol	string
future-option-product	{...}
}
FuturesNestedOptionChainSerializer{
description:	
FuturesNestedOptionChainSerializer model

futures	{...}
option-chains	{...}
}
Cryptocurrency{
description:	
Cryptocurrency model

id	integer($int32)
active	boolean
description	string
instrument-type	string
is-closing-only	boolean
short-description	string
streamer-symbol	string
symbol	string
tick-size	number($double)
}
Equity{
description:	
Equity model

active	boolean
borrow-rate	number($double)
bypass-manual-review	boolean
country-of-incorporation	string
country-of-taxation	string
description	string
halted-at	string($date-time)
instrument-sub-type	string
instrument-type	string
is-closing-only	boolean
is-etf	boolean
is-fractional-quantity-eligible	boolean
is-illiquid	boolean
is-index	boolean
is-options-closing-only	boolean
lendability	string
listed-market	string
market-time-instrument-collection	string
overnight-trading-permitted	boolean
pre-ipo	boolean
short-description	string
stops-trading-at	string($date-time)
streamer-symbol	string
symbol	string
underlying-product-type	string
option-tick-sizes	{...}
tick-sizes	{...}
}
EquityOption{
description:	
EquityOption model

active	boolean
days-to-expiration	integer($int32)
exercise-style	string
expiration-date	string($date)
expiration-type	string
expires-at	string($date-time)
halted-at	string($date-time)
instrument-type	string
is-closing-only	boolean
listed-market	string
market-time-instrument-collection	string
old-security-number	string
option-chain-type	string
option-type	string
root-symbol	string
settlement-type	string
shares-per-contract	integer($int32)
stops-trading-at	string($date-time)
streamer-symbol	string
strike-price	number($double)
symbol	string
underlying-symbol	string
}
Future{
description:	
Future model

active	boolean
active-month	boolean
back-month-first-calendar-symbol	boolean
closing-only-date	string($date)
contract-size	number($double)
display-factor	number($double)
exchange	string
exchange-data	{...}
expiration-date	string($date)
expires-at	string($date-time)
first-notice-date	string($date)
product-group	string
is-closing-only	boolean
last-trade-date	string($date)
main-fraction	number($double)
next-active-month	boolean
notional-multiplier	number($double)
product-code	string
roll-target-symbol	string
security-id	string
stops-trading-at	string($date-time)
streamer-exchange-code	string
streamer-symbol	string
sub-fraction	number($double)
symbol	string
tick-size	number($double)
is-tradeable	boolean
true-underlying-symbol	string
future-etf-equivalent	{...}
future-product	{...}
option-tick-sizes	{...}
spread-tick-sizes	{...}
tick-sizes	{...}
}
FutureOptionProduct{
description:	
FutureOptionProduct model

cash-settled	boolean
code	string
display-factor	number($double)
exchange	string
expiration-type	string
is-am-settled	boolean
itm-rule	string
market-sector	string
product-subtype	string
product-type	string
root-symbol	string
settlement-delay-days	integer($int32)
supported	boolean
}
FutureProduct{
description:	
FutureProduct model

active-months	string
back-month-first-calendar-symbol	boolean
base-tick	integer($int32)
cash-settled	boolean
code	string
contract-limit	integer($int32)
description	string
display-factor	number($double)
exchange	string
first-notice	boolean
listed-months	string
market-sector	string
notional-multiplier	number($double)
price-format	string
product-subtype	string
product-type	string
security-group	string
small-notional	boolean
streamer-exchange-code	string
sub-tick	integer($int32)
supported	boolean
root-symbol	string
tick-size	number($double)
true-underlying-code	string
underlying-description	string
underlying-identifier	string
}
QuantityDecimalPrecision{
description:	
QuantityDecimalPrecision model

instrument-type	string
minimum-increment-precision	integer($int32)
symbol	string
value	integer($int32)
}
SearchableInstrumentDeserializer{
description:	
SearchableInstrumentDeserializer model

category	string
description	string
event-product-external-id	string
exchange	string
expiration-value	string
external-id	string
instrument-type	string
stops-trading-at	string($date-time)
strike-types	string
sub-category	string
symbol	string
underlying-product	string
underlying-product-type	string
underlying-streamer-symbol	string
}
Warrant{
description:	
Warrant model

active	boolean
cusip	string
description	string
instrument-type	string
is-closing-only	boolean
listed-market	string
symbol	string
}
CompactOptionChainSerializer{
description:	
CompactOptionChainSerializer model

underlying-symbol	string
root-symbol	string
option-chain-type	string
settlement-type	string
shares-per-contract	integer($int32)
expiration-type	string
deliverables	{...}
symbols	string
streamer-symbols	string
}
NestedOptionChainSerializer{
description:	
NestedOptionChainSerializer model

underlying-symbol	string
root-symbol	string
option-chain-type	string
shares-per-contract	integer($int32)
tick-sizes	{...}
deliverables	{...}
expirations	{...}
}


Margin Requirements
 11.24.0 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Schemes

https
Filter by tag
margin-requirements
allows a client to fetch margin-requirements for positions and orders



GET
/margin/accounts/{account_number}/requirements
Fetch current margin/captial requirements report for an account

Parameters
Name	Description
account_number *
string
(path)
account_number
Responses
Response content type

application/json
Code	Description
200	
Retrieve account margin/capital requirements report


POST
/margin/accounts/{account_number}/dry-run
Estimate margin requirements for an order given an account

Parameters
Name	Description
account_number *
string
(path)
account_number
order
object
(body)
the order to evaluate for margin requirements

Example Value
Model
{
  "account-number": "string",
  "underlying-symbol": "string",
  "order-type": "string",
  "time-in-force": "string",
  "gtc-date": "2026-08-16",
  "price": "string",
  "price-effect": "Credit",
  "stop-trigger": "string",
  "replaces-order-id": "string",
  "legs": [
    {
      "symbol": "string",
      "instrument-type": "string",
      "quantity": "string",
      "action": "string",
      "remaining-quantity": "string"
    }
  ]
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
200	
Retrieve account margin/capital requirements report

OpenAPI definition
 v0 
OAS3
Servers

http://localhost:8080 - Generated server url
Filter by tag
market-data-controller


GET
/market-data/by-type
Get market data for many symbols of type. Combined limit across all types is 100
Parameters
Name	Description
index
array[string]
(query)
Default value : List []

equity
array[string]
(query)
Default value : List []

equity-option
array[string]
(query)
Default value : List []

future
array[string]
(query)
Default value : List []

future-option
array[string]
(query)
Default value : List []

cryptocurrency
array[string]
(query)
Default value : List []

Responses
Code	Description	Links
200	
successful operation

Media type

*/*
Example Value
Schema
{
  "lastMkt": 0,
  "tradingHalted": true,
  "instrumentType": "Bond",
  "lowLimitPrice": 0,
  "tradingHaltedReason": "string",
  "prevClose": 0,
  "dividendAmount": 0,
  "haltEndTime": 0,
  "instrument": {
    "symbol": "string",
    "instrumentType": "Bond",
    "instrumentKey": {
      "symbol": "string",
      "instrumentType": "Bond"
    },
    "underlyingInstrument": "string",
    "rootSymbol": "string",
    "exchange": "EQUITY"
  },
  "mid": 0,
  "yearHighPrice": 0,
  "open": 0,
  "dayHighPrice": 0,
  "closePriceType": "UNKNOWN",
  "lastExt": 0,
  "dividendFrequency": 0,
  "mark": 0,
  "updatedAt": "2026-08-16T07:10:10.198Z",
  "haltStartTime": 0,
  "highLimitPrice": 0,
  "beta": 0,
  "dayLowPrice": 0,
  "yearLowPrice": 0,
  "prevCloseDate": "2026-08-16",
  "summaryDate": "2026-08-16",
  "last": 0,
  "symbol": "string",
  "close": 0,
  "ask": 0,
  "askSize": 0,
  "volume": 0,
  "lastTradeTime": 0,
  "bid": 0,
  "prevClosePriceType": "UNKNOWN",
  "bidSize": 0
}
No links

Schemas
Instrument{
symbol	string
instrumentType	string
Enum:
Array [ 12 ]
instrumentKey	InstrumentKey{...}
underlyingInstrument	{...}
rootSymbol	string
exchange	string
Enum:
Array [ 9 ]
}
InstrumentKey{
symbol	string
instrumentType	string
Enum:
Array [ 12 ]
}
MarketData{
symbol	string
instrumentType	string
Enum:
Array [ 12 ]
updatedAt	string($date-time)
bid	number($double)
bidSize	number($double)
ask	number($double)
askSize	number($double)
mid	number($double)
mark	number($double)
last	number($double)
lastExt	number($double)
lastMkt	number($double)
beta	number($double)
dividendAmount	number($double)
dividendFrequency	number($double)
open	number($double)
dayHighPrice	number($double)
dayLowPrice	number($double)
close	number($double)
closePriceType	string
Enum:
Array [ 5 ]
prevClose	number($double)
prevClosePriceType	string
Enum:
Array [ 5 ]
summaryDate	string($date)
prevCloseDate	string($date)
lowLimitPrice	number($double)
highLimitPrice	number($double)
tradingHaltedReason	string
haltStartTime	integer($int64)
haltEndTime	integer($int64)
yearLowPrice	number($double)
yearHighPrice	number($double)
volume	number($double)
tradingHalted	boolean
lastTradeTime	integer($int64)
dayOpen	number($double)
deprecated: true
dayHigh	number($double)
deprecated: true
dayLow	number($double)
deprecated: true
dayClose	number($double)
deprecated: true
prevDayClose	number($double)
deprecated: true
instrument	Instrument{...}
}


Market Metrics
 1.9.1 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
default


GET
/market-metrics
Returns an array of volatility data for given symbols.

Parameters
Name	Description
symbols *
string
(query)
Comma separated list of symbols i.e. ?symbols=AAPL,FB,BRK%2FB

symbols
Responses
Response content type

application/json
Code	Description
200	
Returns an array of volatility data for given symbols.

Example Value
Model
[
  {
    "implied-volatility-percentile": 0,
    "liquidity-rank": 0,
    "option-expiration-implied-volatilities": [
      {
        "expiration-date": "2026-08-16T07:10:31.706Z",
        "settlement-type": "string",
        "option-chain-type": "string",
        "implied-volatility": 0
      }
    ],
    "implied-volatility-rank": 0,
    "implied-volatility-index": 0,
    "liquidity": 0,
    "implied-volatility-index-5-day-change": 0,
    "symbol": "string",
    "liquidity-rating": 0
  }
]

GET
/market-metrics/historic-corporate-events/dividends/{symbol}
Get historical dividend data

Parameters
Name	Description
symbol *
string
(path)
Symbol to get dividends data for

symbol
Responses
Response content type

application/json
Code	Description
200	
Array of historical dividend data

Example Value
Model
[
  {
    "occurred-date": "2026-08-16",
    "amount": 0
  }
]

GET
/market-metrics/historic-corporate-events/earnings-reports/{symbol}
Get historical earnings data

Parameters
Name	Description
symbol *
string
(path)
Symbol to get earnings data for

symbol
start-date *
string($date)
(query)
Limits earnings data from start-date until now

start-date
end-date
string($date)
(query)
Limits earnings data from start-date until end-date

end-date
Responses
Response content type

application/json
Code	Description
200	
Array of historical earnings data

Example Value
Model
[
  {
    "occurred-date": "2026-08-16",
    "eps": 0
  }
]

Models
DividendInfo{
occurred-date	string($date)
Date of dividend

amount	number($float)
Per share amount

}
EarningsInfo{
occurred-date	string($date)
Date of earnings announcement

eps	number($float)
Earnings per share amount

}
MarketMetricInfo{
description:	
Includes underlying volatility and liquidity data as well as option volatility data.

symbol	string
Symbol

implied-volatility-index	number($float)
IV Index of underlying

implied-volatility-index-5-day-change	number($float)
5 day change of IV index of underlying

implied-volatility-rank	number($float)
IV Rank of underlying

implied-volatility-percentile	number($float)
IV percentile of underlying

liquidity	number($float)
liquidity of underlying

liquidity-rank	number($float)
liquidity rank of underlying

liquidity-rating	number($int32)
liquidity rating of underlying

option-expiration-implied-volatilities	[...]
}

Market Sessions
 2.0.1 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
market-time
Operations about market-times



GET
/market-time/sessions
Retrieve a list of session timings for a date range

Parameters
Name	Description
from-date
string($date)
(query)
The date to start from

Default value : 2026-04-15

2026-04-15
instrument-collection
string
(query)
The instrument collection to get sessions for

Available values : Equity, CME, CFE

Default value : Equity


Equity
to-date *
string($date)
(query)
The date to end at, the difference between from and to date must not exceed 9 months

to-date
Responses
Response content type

application/json
Code	Description
200	
Retrieve a list of session timings for a date range

Example Value
Model
[
  {
    "close-at": "2026-08-16T07:11:01.445Z",
    "close-at-ext": "2026-08-16T07:11:01.445Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.445Z",
    "start-at": "2026-08-16T07:11:01.445Z"
  }
]

GET
/market-time/sessions/current
Retrieve the current session timings for an instrument collection

Parameters
Name	Description
instrument-collections *
array[string]
(query)
List of instrument collections . i.e. CFE, CME or Equity (example: instrument-collections[]={value1}&instrument-collections[]={value2})

Available values : CFE, CME, Equity

CFECMEEquity
Responses
Response content type

application/json
Code	Description
200	
Retrieve the current session timings for an instrument collection

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.447Z",
  "close-at-ext": "2026-08-16T07:11:01.447Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.447Z",
  "start-at": "2026-08-16T07:11:01.447Z",
  "next-session": {
    "close-at": "2026-08-16T07:11:01.447Z",
    "close-at-ext": "2026-08-16T07:11:01.447Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.447Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.447Z"
  },
  "previous-session": {
    "close-at": "2026-08-16T07:11:01.447Z",
    "close-at-ext": "2026-08-16T07:11:01.447Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.447Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.447Z"
  },
  "state": "string"
}

GET
/market-time/equities/sessions/current
Get current equities session

Parameters
Name	Description
current-time
string($date-time)
(query)
The time the current session is based on

current-time
Responses
Response content type

application/json
Code	Description
200	
Get current equities session

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.449Z",
  "close-at-ext": "2026-08-16T07:11:01.449Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.449Z",
  "start-at": "2026-08-16T07:11:01.449Z",
  "next-session": {
    "close-at": "2026-08-16T07:11:01.449Z",
    "close-at-ext": "2026-08-16T07:11:01.449Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.449Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.449Z"
  },
  "previous-session": {
    "close-at": "2026-08-16T07:11:01.449Z",
    "close-at-ext": "2026-08-16T07:11:01.449Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.449Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.449Z"
  },
  "state": "string"
}

GET
/market-time/equities/sessions/next
Get next equities session or session after date

Parameters
Name	Description
date
string($date)
(query)
Find session on or after this date

Default value : 2026-04-15

2026-04-15
Responses
Response content type

application/json
Code	Description
200	
Get next equities session or session after date

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.454Z",
  "close-at-ext": "2026-08-16T07:11:01.454Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.454Z",
  "session-date": "2026-08-16",
  "start-at": "2026-08-16T07:11:01.454Z"
}

GET
/market-time/equities/sessions/previous
Get previous session or session before date

Parameters
Name	Description
date
string($date)
(query)
Find session before this date

Default value : 2026-04-15

2026-04-15
Responses
Response content type

application/json
Code	Description
200	
Get previous session or session before date

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.457Z",
  "close-at-ext": "2026-08-16T07:11:01.457Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.457Z",
  "session-date": "2026-08-16",
  "start-at": "2026-08-16T07:11:01.457Z"
}

GET
/market-time/equities/holidays
Get equity holidays

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Get equity holidays

Example Value
Model
[
  {
    "market-half-days": {},
    "market-holidays": {}
  }
]

GET
/market-time/futures/sessions/current
Get current futures sessions

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Get current futures sessions

Example Value
Model
[
  {
    "close-at": "2026-08-16T07:11:01.459Z",
    "close-at-ext": "2026-08-16T07:11:01.459Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.459Z",
    "start-at": "2026-08-16T07:11:01.459Z",
    "next-session": {
      "close-at": "2026-08-16T07:11:01.459Z",
      "close-at-ext": "2026-08-16T07:11:01.459Z",
      "instrument-collection": "string",
      "open-at": "2026-08-16T07:11:01.459Z",
      "session-date": "2026-08-16",
      "start-at": "2026-08-16T07:11:01.459Z"
    },
    "previous-session": {
      "close-at": "2026-08-16T07:11:01.459Z",
      "close-at-ext": "2026-08-16T07:11:01.459Z",
      "instrument-collection": "string",
      "open-at": "2026-08-16T07:11:01.459Z",
      "session-date": "2026-08-16",
      "start-at": "2026-08-16T07:11:01.459Z"
    },
    "state": "string"
  }
]

GET
/market-time/futures/sessions/current/{instrument_collection}
Get current futures session for exchange

Parameters
Name	Description
instrument_collection *
string
(path)
Futures exchange, one of: CFE, CME

Available values : CFE, CME


CFE
Responses
Response content type

application/json
Code	Description
200	
Get current futures session for exchange

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.461Z",
  "close-at-ext": "2026-08-16T07:11:01.461Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.461Z",
  "start-at": "2026-08-16T07:11:01.461Z",
  "next-session": {
    "close-at": "2026-08-16T07:11:01.461Z",
    "close-at-ext": "2026-08-16T07:11:01.461Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.461Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.461Z"
  },
  "previous-session": {
    "close-at": "2026-08-16T07:11:01.461Z",
    "close-at-ext": "2026-08-16T07:11:01.461Z",
    "instrument-collection": "string",
    "open-at": "2026-08-16T07:11:01.461Z",
    "session-date": "2026-08-16",
    "start-at": "2026-08-16T07:11:01.461Z"
  },
  "state": "string"
}

GET
/market-time/futures/sessions/next/{instrument_collection}
Get next futures session for exchange

Parameters
Name	Description
instrument_collection *
string
(path)
Futures exchange, one of: CFE, CME

Available values : CFE, CME


CFE
date
string($date)
(query)
Find session on or after this date

Default value : 2026-04-15

2026-04-15
Responses
Response content type

application/json
Code	Description
200	
Get next futures session for exchange

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.464Z",
  "close-at-ext": "2026-08-16T07:11:01.464Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.464Z",
  "session-date": "2026-08-16",
  "start-at": "2026-08-16T07:11:01.464Z"
}

GET
/market-time/futures/sessions/previous/{instrument_collection}
Get previous futures sessions for exchange

Parameters
Name	Description
instrument_collection *
string
(path)
Futures exchange, one of: CFE, CME

Available values : CFE, CME


CFE
date
string($date)
(query)
Find session before this date

Default value : 2026-04-15

2026-04-15
Responses
Response content type

application/json
Code	Description
200	
Get previous futures sessions for exchange

Example Value
Model
{
  "close-at": "2026-08-16T07:11:01.467Z",
  "close-at-ext": "2026-08-16T07:11:01.467Z",
  "instrument-collection": "string",
  "open-at": "2026-08-16T07:11:01.467Z",
  "session-date": "2026-08-16",
  "start-at": "2026-08-16T07:11:01.467Z"
}

GET
/market-time/futures/holidays/{instrument_collection}
Get future holidays for exchange

Parameters
Name	Description
instrument_collection *
string
(path)
Futures exchange, one of: CFE, CME

Available values : CFE, CME


CFE
Responses
Response content type

application/json
Code	Description
200	
Get future holidays for exchange

Example Value
Model
[
  {
    "market-half-days": {},
    "market-holidays": {}
  }
]

Models
SimpleSessionDeserializer{
description:	
SimpleSessionDeserializer model

close-at	string($date-time)
close-at-ext	string($date-time)
instrument-collection	string
open-at	string($date-time)
start-at	string($date-time)
}
CurrentSessionDeserializer{
description:	
CurrentSessionDeserializer model

close-at	string($date-time)
close-at-ext	string($date-time)
instrument-collection	string
open-at	string($date-time)
start-at	string($date-time)
next-session	{...}
previous-session	{...}
state	string
}
CurrentSession{
description:	
CurrentSession model

close-at	string($date-time)
close-at-ext	string($date-time)
instrument-collection	string
open-at	string($date-time)
start-at	string($date-time)
next-session	{...}
previous-session	{...}
state	string
}
NextSession{
description:	
NextSession model

close-at	string($date-time)
close-at-ext	string($date-time)
instrument-collection	string
open-at	string($date-time)
session-date	string($date)
start-at	string($date-time)
}
PreviousSession{
description:	
PreviousSession model

close-at	string($date-time)
close-at-ext	string($date-time)
instrument-collection	string
open-at	string($date-time)
session-date	string($date)
start-at	string($date-time)
}
MarketCalendarDeserializer{
description:	
MarketCalendarDeserializer model

market-half-days	{...}
market-holidays	{...}
}

OpenAPI definition
 v0 
OAS3
Servers

http://localhost:8080 - Generated server url
Filter by tag
net-liq-controller


GET
/accounts/{accountNumber}/net-liq/history
Get net-liq history for account
Parameters
Name	Description
accountNumber *
string
(path)
accountNumber
time-back
string
(query)
A request of data relative to the current time

Available values : 1d, 1w, 1m, 3m, 6m, 1y, all


--
start-time
string
(query)
The start-time of a windowed request in format '2011-12-03T10:15:30+01:00[Europe/Paris]'

start-time
end-time
string
(query)
The end-time of a windowed request in format '2011-12-03T10:15:30+01:00[Europe/Paris]'

end-time
interval
string
(query)
interval
Responses
Code	Description	Links
200	
successful operation

Media type

*/*
Example Value
Schema
{
  "high": 0,
  "totalHigh": 0,
  "pendingCashLow": 0,
  "open": 0,
  "pendingCashHigh": 0,
  "time": "string",
  "pendingCashClose": 0,
  "totalOpen": 0,
  "totalLow": 0,
  "totalClose": 0,
  "pendingCashOpen": 0,
  "low": 0,
  "close": 0
}
No links

Schemas
NetLiqOhlc{
open	number($double)
high	number($double)
low	number($double)
close	number($double)
totalOpen	number($double)
totalHigh	number($double)
totalLow	number($double)
totalClose	number($double)
pendingCashOpen	number($double)
pendingCashHigh	number($double)
pendingCashLow	number($double)
pendingCashClose	number($double)
time	string
}

Orders
 0.0.1 
[ Base URL: api.tastyworks.com ]

Api Version 20260427
Download Open-api Json
For this API version, you'll need to include a 'Accept-Version' header with a value of '20260427'. If you leave out that header, you will hit the default API version (20250812).
Filter by tag
complex-orders
Allows an API client to retreive information about complex orders on a per account basis.



GET
/accounts/{account_number}/complex-orders
Returns a paginated list of all Complex Orders

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 10

10
Responses
Response content type

application/json
Code	Description
200	
Returns a paginated list of all Complex Orders

Example Value
Model
[
  {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.484Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.484Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.484Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.484Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.484Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.484Z",
          "cancelled-at": "2026-08-16T07:12:09.484Z",
          "route-after": "2026-08-16T07:12:09.484Z",
          "routed-at": "2026-08-16T07:12:09.484Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.484Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.484Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.484Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.484Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.484Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.484Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.484Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.484Z",
        "cancelled-at": "2026-08-16T07:12:09.484Z",
        "route-after": "2026-08-16T07:12:09.484Z",
        "routed-at": "2026-08-16T07:12:09.484Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.484Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.484Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  }
]

POST
/accounts/{account_number}/complex-orders
Creates a new Complex Order from supplied params

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
postAccountsAccountNumberComplexOrders *
object
(body)
Example Value
Model
{
  "orders": [
    {
      "gtc-date": "2026-08-16",
      "order-type": "Limit",
      "stop-trigger": 0,
      "time-in-force": "Day",
      "price": 0,
      "price-effect": "Credit",
      "value": 0,
      "value-effect": "Credit",
      "automated-source": false,
      "external-identifier": "string",
      "partition-key": "string",
      "preflight-id": "string",
      "source": "string",
      "legs": [
        {
          "action": "Allocate",
          "instrument-type": "Cryptocurrency",
          "quantity": 0,
          "symbol": "string"
        }
      ],
      "rules": {
        "cancel-at": "2026-08-16T07:12:08.438Z",
        "conditions": [
          {
            "action": "cancel",
            "instrument-type": "Bond",
            "symbol": "string",
            "comparator": "gte",
            "indicator": "last",
            "threshold": 0,
            "price-components": [
              {
                "instrument-type": "Bond",
                "quantity": 0,
                "quantity-direction": "Long",
                "symbol": "string"
              }
            ]
          }
        ],
        "route-after": "2026-08-16T07:12:08.438Z"
      },
      "advanced-instructions": {
        "strict-position-effect-validation": false
      }
    }
  ],
  "trigger-order": {
    "gtc-date": "2026-08-16",
    "order-type": "Limit",
    "stop-trigger": 0,
    "time-in-force": "Day",
    "price": 0,
    "price-effect": "Credit",
    "value": 0,
    "value-effect": "Credit",
    "automated-source": false,
    "external-identifier": "string",
    "partition-key": "string",
    "preflight-id": "string",
    "source": "string",
    "legs": [
      {
        "action": "Allocate",
        "instrument-type": "Cryptocurrency",
        "quantity": 0,
        "symbol": "string"
      }
    ],
    "rules": {
      "cancel-at": "2026-08-16T07:12:08.438Z",
      "conditions": [
        {
          "action": "cancel",
          "instrument-type": "Bond",
          "symbol": "string",
          "comparator": "gte",
          "indicator": "last",
          "threshold": 0,
          "price-components": [
            {
              "instrument-type": "Bond",
              "quantity": 0,
              "quantity-direction": "Long",
              "symbol": "string"
            }
          ]
        }
      ],
      "route-after": "2026-08-16T07:12:08.438Z"
    },
    "advanced-instructions": {
      "strict-position-effect-validation": false
    }
  },
  "type": "BLAST",
  "ratio-price-comparator": "gte",
  "ratio-price-is-threshold-based-on-notional": true,
  "ratio-price-threshold": 0,
  "source": "string"
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Creates a new Complex Order from supplied params

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.499Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.499Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.499Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.499Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.499Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.499Z",
      "cancelled-at": "2026-08-16T07:12:09.499Z",
      "route-after": "2026-08-16T07:12:09.499Z",
      "routed-at": "2026-08-16T07:12:09.499Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.499Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.499Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.499Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.499Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.499Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.499Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.499Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.499Z",
          "cancelled-at": "2026-08-16T07:12:09.499Z",
          "route-after": "2026-08-16T07:12:09.499Z",
          "routed-at": "2026-08-16T07:12:09.499Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.499Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.499Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.499Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.499Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.499Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.499Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.499Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.499Z",
        "cancelled-at": "2026-08-16T07:12:09.499Z",
        "route-after": "2026-08-16T07:12:09.499Z",
        "routed-at": "2026-08-16T07:12:09.499Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.499Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.499Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

POST
/accounts/{account_number}/complex-orders/dry-run
Performs a dry-run for a new ComplexOrder from supplied params. Allows validation of potential orders.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
postAccountsAccountNumberComplexOrdersDryRun *
object
(body)
Example Value
Model
{
  "orders": [
    {
      "gtc-date": "2026-08-16",
      "order-type": "Limit",
      "stop-trigger": 0,
      "time-in-force": "Day",
      "price": 0,
      "price-effect": "Credit",
      "value": 0,
      "value-effect": "Credit",
      "automated-source": false,
      "external-identifier": "string",
      "partition-key": "string",
      "preflight-id": "string",
      "source": "string",
      "legs": [
        {
          "action": "Allocate",
          "instrument-type": "Cryptocurrency",
          "quantity": 0,
          "symbol": "string"
        }
      ],
      "rules": {
        "cancel-at": "2026-08-16T07:12:08.440Z",
        "conditions": [
          {
            "action": "cancel",
            "instrument-type": "Bond",
            "symbol": "string",
            "comparator": "gte",
            "indicator": "last",
            "threshold": 0,
            "price-components": [
              {
                "instrument-type": "Bond",
                "quantity": 0,
                "quantity-direction": "Long",
                "symbol": "string"
              }
            ]
          }
        ],
        "route-after": "2026-08-16T07:12:08.440Z"
      },
      "advanced-instructions": {
        "strict-position-effect-validation": false
      }
    }
  ],
  "trigger-order": {
    "gtc-date": "2026-08-16",
    "order-type": "Limit",
    "stop-trigger": 0,
    "time-in-force": "Day",
    "price": 0,
    "price-effect": "Credit",
    "value": 0,
    "value-effect": "Credit",
    "automated-source": false,
    "external-identifier": "string",
    "partition-key": "string",
    "preflight-id": "string",
    "source": "string",
    "legs": [
      {
        "action": "Allocate",
        "instrument-type": "Cryptocurrency",
        "quantity": 0,
        "symbol": "string"
      }
    ],
    "rules": {
      "cancel-at": "2026-08-16T07:12:08.440Z",
      "conditions": [
        {
          "action": "cancel",
          "instrument-type": "Bond",
          "symbol": "string",
          "comparator": "gte",
          "indicator": "last",
          "threshold": 0,
          "price-components": [
            {
              "instrument-type": "Bond",
              "quantity": 0,
              "quantity-direction": "Long",
              "symbol": "string"
            }
          ]
        }
      ],
      "route-after": "2026-08-16T07:12:08.440Z"
    },
    "advanced-instructions": {
      "strict-position-effect-validation": false
    }
  },
  "type": "BLAST",
  "ratio-price-comparator": "gte",
  "ratio-price-is-threshold-based-on-notional": true,
  "ratio-price-threshold": 0,
  "source": "string"
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Performs a dry-run for a new ComplexOrder from supplied params. Allows validation of potential orders.

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.513Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.513Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.513Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.513Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.513Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.513Z",
      "cancelled-at": "2026-08-16T07:12:09.513Z",
      "route-after": "2026-08-16T07:12:09.513Z",
      "routed-at": "2026-08-16T07:12:09.513Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.513Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.513Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.513Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.513Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.513Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.513Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.513Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.513Z",
          "cancelled-at": "2026-08-16T07:12:09.513Z",
          "route-after": "2026-08-16T07:12:09.513Z",
          "routed-at": "2026-08-16T07:12:09.513Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.513Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.513Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.513Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.513Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.513Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.513Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.513Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.513Z",
        "cancelled-at": "2026-08-16T07:12:09.513Z",
        "route-after": "2026-08-16T07:12:09.513Z",
        "routed-at": "2026-08-16T07:12:09.513Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.513Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.513Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

GET
/accounts/{account_number}/complex-orders/live
Returns all Complex Orders where a compenent order was placed today

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
Responses
Response content type

application/json
Code	Description
200	
Returns all Complex Orders where a compenent order was placed today

Example Value
Model
[
  {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.524Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.524Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.524Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.524Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.524Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.524Z",
          "cancelled-at": "2026-08-16T07:12:09.524Z",
          "route-after": "2026-08-16T07:12:09.524Z",
          "routed-at": "2026-08-16T07:12:09.524Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.524Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.524Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.524Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.524Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.524Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.524Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.524Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.524Z",
        "cancelled-at": "2026-08-16T07:12:09.524Z",
        "route-after": "2026-08-16T07:12:09.524Z",
        "routed-at": "2026-08-16T07:12:09.524Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.524Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.524Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  }
]

GET
/accounts/{account_number}/complex-orders/{id}
Returns a full representation of a Complex Order

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
Responses
Response content type

application/json
Code	Description
200	
Returns a full representation of a Complex Order

Example Value
Model
{
  "terminal-at": "string",
  "orders": [
    {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.534Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.534Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.534Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.534Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.534Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.534Z",
        "cancelled-at": "2026-08-16T07:12:09.534Z",
        "route-after": "2026-08-16T07:12:09.534Z",
        "routed-at": "2026-08-16T07:12:09.534Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.534Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.534Z"
    }
  ],
  "ratio-price-threshold": 0,
  "related-orders": [
    {
      "id": "string",
      "complex-order-id": "string",
      "complex-order-tag": "string",
      "replaces-order-id": "string",
      "replacing-order-id": "string",
      "status": "string"
    }
  ],
  "account-number": "string",
  "ratio-price-is-threshold-based-on-notional": true,
  "trigger-order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.534Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.534Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.534Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.534Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.534Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.534Z",
      "cancelled-at": "2026-08-16T07:12:09.534Z",
      "route-after": "2026-08-16T07:12:09.534Z",
      "routed-at": "2026-08-16T07:12:09.534Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.534Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.534Z"
  },
  "type": "string",
  "id": "string",
  "ratio-price-comparator": "string"
}

PATCH
/accounts/{account_number}/complex-orders/{id}
Edit threshold-price of a PAIRS trade.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
patchAccountsAccountNumberComplexOrdersId *
object
(body)
Example Value
Model
{
  "ratio-price-comparator": "gte",
  "ratio-price-threshold": 0
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
200	
Edit threshold-price of a PAIRS trade.

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.544Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.544Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.544Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.544Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.544Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.544Z",
      "cancelled-at": "2026-08-16T07:12:09.544Z",
      "route-after": "2026-08-16T07:12:09.544Z",
      "routed-at": "2026-08-16T07:12:09.544Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.544Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.544Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.544Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.544Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.544Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.544Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.544Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.544Z",
          "cancelled-at": "2026-08-16T07:12:09.544Z",
          "route-after": "2026-08-16T07:12:09.544Z",
          "routed-at": "2026-08-16T07:12:09.544Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.544Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.544Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.544Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.544Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.544Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.544Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.544Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.544Z",
        "cancelled-at": "2026-08-16T07:12:09.544Z",
        "route-after": "2026-08-16T07:12:09.544Z",
        "routed-at": "2026-08-16T07:12:09.544Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.544Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.544Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

DELETE
/accounts/{account_number}/complex-orders/{id}
Request cancellation for all non terminal components of a Complex Order

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
Responses
Response content type

application/json
Code	Description
200	
Request cancellation for all non terminal components of a Complex Order

Example Value
Model
{
  "terminal-at": "string",
  "orders": [
    {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.555Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.555Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.555Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.555Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.555Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.555Z",
        "cancelled-at": "2026-08-16T07:12:09.555Z",
        "route-after": "2026-08-16T07:12:09.555Z",
        "routed-at": "2026-08-16T07:12:09.555Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.555Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.555Z"
    }
  ],
  "ratio-price-threshold": 0,
  "related-orders": [
    {
      "id": "string",
      "complex-order-id": "string",
      "complex-order-tag": "string",
      "replaces-order-id": "string",
      "replacing-order-id": "string",
      "status": "string"
    }
  ],
  "account-number": "string",
  "ratio-price-is-threshold-based-on-notional": true,
  "trigger-order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.555Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.555Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.555Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.555Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.555Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.555Z",
      "cancelled-at": "2026-08-16T07:12:09.555Z",
      "route-after": "2026-08-16T07:12:09.556Z",
      "routed-at": "2026-08-16T07:12:09.556Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.556Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.556Z"
  },
  "type": "string",
  "id": "string",
  "ratio-price-comparator": "string"
}

POST
/accounts/{account_number}/complex-orders/{id}/dry-run
Performs a dry-run for editing a ComplexOrder from supplied params.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
postAccountsAccountNumberComplexOrdersIdDryRun *
object
(body)
Example Value
Model
{
  "ratio-price-comparator": "gte",
  "ratio-price-threshold": 0
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Performs a dry-run for editing a ComplexOrder from supplied params.

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.564Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.564Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.564Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.564Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.564Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.564Z",
      "cancelled-at": "2026-08-16T07:12:09.564Z",
      "route-after": "2026-08-16T07:12:09.564Z",
      "routed-at": "2026-08-16T07:12:09.564Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.564Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.564Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.564Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.564Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.564Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.564Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.564Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.564Z",
          "cancelled-at": "2026-08-16T07:12:09.564Z",
          "route-after": "2026-08-16T07:12:09.564Z",
          "routed-at": "2026-08-16T07:12:09.564Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.564Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.564Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.564Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.564Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.564Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.564Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.564Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.564Z",
        "cancelled-at": "2026-08-16T07:12:09.564Z",
        "route-after": "2026-08-16T07:12:09.564Z",
        "routed-at": "2026-08-16T07:12:09.564Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.565Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.565Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}
orders
Allows an API client to view, filter, create, cancel and replace orders.



GET
/accounts/{account_number}/orders
Returns a paginated list of the customer's orders (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 100

100
end-at
string($date-time)
(query)
DateTime end range for filtering orders in full date-time

end-at
end-date
string($date)
(query)
end-date
futures-symbol
string
(query)
Used to fetch both futures and futures options orders

futures-symbol
sort
string
(query)
The order to sort results in. Accepts Desc or Asc. Defaults to Desc

Available values : Desc, Asc

Default value : Desc


Desc
start-at
string($date-time)
(query)
DateTime start rage for filtering orders in full date-time. Required if start-date param not provided

start-at
start-date
string($date)
(query)
start-date
status
array[string]
(query)
(example: status[]={value1}&status[]={value2})

underlying-instrument-type
string
(query)
Underlying instrument type

underlying-instrument-type
underlying-symbol
string
(query)
underlying-symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a paginated list of the customer's orders (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Example Value
Model
[
  {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.584Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.584Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.584Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.584Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.584Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.584Z",
      "cancelled-at": "2026-08-16T07:12:09.584Z",
      "route-after": "2026-08-16T07:12:09.584Z",
      "routed-at": "2026-08-16T07:12:09.584Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.584Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.584Z"
  }
]

POST
/accounts/{account_number}/orders
Accepts a json document containing parameters to create an order for the client.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
postAccountsAccountNumberOrders *
object
(body)
Example Value
Model
{
  "gtc-date": "2026-08-16",
  "order-type": "Limit",
  "stop-trigger": 0,
  "time-in-force": "Day",
  "price": 0,
  "price-effect": "Credit",
  "value": 0,
  "value-effect": "Credit",
  "automated-source": false,
  "external-identifier": "string",
  "partition-key": "string",
  "preflight-id": "string",
  "source": "string",
  "legs": [
    {
      "action": "Allocate",
      "instrument-type": "Cryptocurrency",
      "quantity": 0,
      "symbol": "string"
    }
  ],
  "rules": {
    "cancel-at": "2026-08-16T07:12:00.574Z",
    "conditions": [
      {
        "action": "cancel",
        "instrument-type": "Bond",
        "symbol": "string",
        "comparator": "gte",
        "indicator": "last",
        "threshold": 0,
        "price-components": [
          {
            "instrument-type": "Bond",
            "quantity": 0,
            "quantity-direction": "Long",
            "symbol": "string"
          }
        ]
      }
    ],
    "route-after": "2026-08-16T07:12:00.574Z"
  },
  "advanced-instructions": {
    "strict-position-effect-validation": false
  }
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Accepts a json document containing parameters to create an order for the client.

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.591Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.591Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.591Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.591Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.591Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.591Z",
      "cancelled-at": "2026-08-16T07:12:09.591Z",
      "route-after": "2026-08-16T07:12:09.591Z",
      "routed-at": "2026-08-16T07:12:09.591Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.591Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.591Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.591Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.591Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.591Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.591Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.591Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.591Z",
          "cancelled-at": "2026-08-16T07:12:09.591Z",
          "route-after": "2026-08-16T07:12:09.591Z",
          "routed-at": "2026-08-16T07:12:09.591Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.591Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.591Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.591Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.591Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.591Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.591Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.591Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.591Z",
        "cancelled-at": "2026-08-16T07:12:09.591Z",
        "route-after": "2026-08-16T07:12:09.591Z",
        "routed-at": "2026-08-16T07:12:09.591Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.591Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.591Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

POST
/accounts/{account_number}/orders/dry-run
Accepts a json document containing parameters to create an order and
then runs the prefights without placing the order.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
postAccountsAccountNumberOrdersDryRun *
object
(body)
Example Value
Model
{
  "gtc-date": "2026-08-16",
  "order-type": "Limit",
  "stop-trigger": 0,
  "time-in-force": "Day",
  "price": 0,
  "price-effect": "Credit",
  "value": 0,
  "value-effect": "Credit",
  "automated-source": false,
  "external-identifier": "string",
  "partition-key": "string",
  "preflight-id": "string",
  "source": "string",
  "legs": [
    {
      "action": "Allocate",
      "instrument-type": "Cryptocurrency",
      "quantity": 0,
      "symbol": "string"
    }
  ],
  "rules": {
    "cancel-at": "2026-08-16T07:11:59.480Z",
    "conditions": [
      {
        "action": "cancel",
        "instrument-type": "Bond",
        "symbol": "string",
        "comparator": "gte",
        "indicator": "last",
        "threshold": 0,
        "price-components": [
          {
            "instrument-type": "Bond",
            "quantity": 0,
            "quantity-direction": "Long",
            "symbol": "string"
          }
        ]
      }
    ],
    "route-after": "2026-08-16T07:11:59.480Z"
  },
  "advanced-instructions": {
    "strict-position-effect-validation": false
  }
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Accepts a json document containing parameters to create an order and
then runs the prefights without placing the order.

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.604Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.604Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.604Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.604Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.604Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.604Z",
      "cancelled-at": "2026-08-16T07:12:09.604Z",
      "route-after": "2026-08-16T07:12:09.604Z",
      "routed-at": "2026-08-16T07:12:09.604Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.604Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.604Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.604Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.604Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.604Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.604Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.604Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.604Z",
          "cancelled-at": "2026-08-16T07:12:09.604Z",
          "route-after": "2026-08-16T07:12:09.604Z",
          "routed-at": "2026-08-16T07:12:09.604Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.604Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.604Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.604Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.604Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.604Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.604Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.604Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.604Z",
        "cancelled-at": "2026-08-16T07:12:09.604Z",
        "route-after": "2026-08-16T07:12:09.604Z",
        "routed-at": "2026-08-16T07:12:09.604Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.604Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.604Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

GET
/accounts/{account_number}/orders/live
Returns a list of orders from the current trading day. Includes all statuses, not only Live.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 100

100
status
string
(query)
Available values : Cancelled, Cancel Requested, Contingent, Expired, Filled, In Flight, Live, Partially Removed, Received, Rejected, Removed, Replace Requested, Routed


--
underlying-symbol
string
(query)
underlying-symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a list of orders from the current trading day. Includes all statuses, not only Live.

Example Value
Model
[
  {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.620Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.620Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.620Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.620Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.620Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.620Z",
      "cancelled-at": "2026-08-16T07:12:09.620Z",
      "route-after": "2026-08-16T07:12:09.620Z",
      "routed-at": "2026-08-16T07:12:09.620Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.620Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.620Z"
  }
]

GET
/accounts/{account_number}/orders/{id}
Returns a single order based on the id

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
Responses
Response content type

application/json
Code	Description
200	
Returns a single order based on the id

Example Value
Model
{
  "size": "string",
  "global-request-id": "string",
  "time-in-force": "string",
  "terminal-at": "2026-08-16T07:12:09.626Z",
  "editable": true,
  "contingent-status": "string",
  "legs": [
    {
      "action": "string",
      "instrument-type": "string",
      "quantity": "string",
      "remaining-quantity": "string",
      "symbol": "string",
      "fills": [
        {
          "destination-venue": "string",
          "ext-exec-id": "string",
          "ext-group-fill-id": "string",
          "fill-id": "string",
          "fill-price": 0,
          "filled-at": "2026-08-16T07:12:09.626Z",
          "quantity": "string"
        }
      ]
    }
  ],
  "gtc-date": "2026-08-16",
  "updated-at": "string",
  "in-flight-at": "2026-08-16T07:12:09.626Z",
  "replaces-order-id": "string",
  "underlying-symbol": "string",
  "edited": true,
  "price": 0,
  "cancel-username": "string",
  "account-number": "string",
  "cancel-user-id": "string",
  "external-identifier": "string",
  "cancellable": true,
  "value-effect": "string",
  "stop-trigger": "string",
  "cancelled-at": "2026-08-16T07:12:09.626Z",
  "leg-count": "string",
  "underlying-instrument-type": "string",
  "value": 0,
  "reject-reason": "string",
  "status": "string",
  "live-at": "2026-08-16T07:12:09.626Z",
  "preflight-id": "string",
  "price-effect": "string",
  "username": "string",
  "replacing-order-id": "string",
  "complex-order-id": "string",
  "order-type": "string",
  "source": "string",
  "id": "string",
  "order-rule": {
    "cancel-at": "2026-08-16T07:12:09.626Z",
    "cancelled-at": "2026-08-16T07:12:09.626Z",
    "route-after": "2026-08-16T07:12:09.626Z",
    "routed-at": "2026-08-16T07:12:09.626Z",
    "order-conditions": [
      {
        "instrument-type": "string",
        "triggered-at": "2026-08-16T07:12:09.626Z",
        "threshold": 0,
        "is-threshold-based-on-notional": true,
        "indicator": "string",
        "comparator": "string",
        "action": "string",
        "symbol": "string",
        "price-components": [
          {
            "instrument-type": "string",
            "quantity": "string",
            "quantity-direction": "string",
            "symbol": "string"
          }
        ],
        "triggered-value": 0,
        "id": "string"
      }
    ]
  },
  "user-id": "string",
  "cancelled-size": 0,
  "complex-order-tag": "string",
  "received-at": "2026-08-16T07:12:09.626Z"
}

PUT
/accounts/{account_number}/orders/{id}
Replaces a live order with a new one. Subsequent fills of the original order will abort the replacement.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
putAccountsAccountNumberOrdersId *
object
(body)
Example Value
Model
{
  "gtc-date": "2026-08-16",
  "order-type": "Limit",
  "stop-trigger": 0,
  "time-in-force": "Day",
  "price": 0,
  "price-effect": "Credit",
  "value": 0,
  "value-effect": "Credit",
  "automated-source": false,
  "external-identifier": "string",
  "partition-key": "string",
  "preflight-id": "string",
  "source": "string",
  "rules": {
    "cancel-at": "2026-08-16T07:11:56.610Z",
    "conditions": [
      {
        "action": "cancel",
        "instrument-type": "Bond",
        "symbol": "string",
        "comparator": "gte",
        "indicator": "last",
        "threshold": 0,
        "price-components": [
          {
            "instrument-type": "Bond",
            "quantity": 0,
            "quantity-direction": "Long",
            "symbol": "string"
          }
        ]
      }
    ],
    "route-after": "2026-08-16T07:11:56.610Z"
  }
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
200	
Replaces a live order with a new one. Subsequent fills of the original order will abort the replacement.

Example Value
Model
{
  "size": "string",
  "global-request-id": "string",
  "time-in-force": "string",
  "terminal-at": "2026-08-16T07:12:09.633Z",
  "editable": true,
  "contingent-status": "string",
  "legs": [
    {
      "action": "string",
      "instrument-type": "string",
      "quantity": "string",
      "remaining-quantity": "string",
      "symbol": "string",
      "fills": [
        {
          "destination-venue": "string",
          "ext-exec-id": "string",
          "ext-group-fill-id": "string",
          "fill-id": "string",
          "fill-price": 0,
          "filled-at": "2026-08-16T07:12:09.633Z",
          "quantity": "string"
        }
      ]
    }
  ],
  "gtc-date": "2026-08-16",
  "updated-at": "string",
  "in-flight-at": "2026-08-16T07:12:09.633Z",
  "replaces-order-id": "string",
  "underlying-symbol": "string",
  "edited": true,
  "price": 0,
  "cancel-username": "string",
  "account-number": "string",
  "cancel-user-id": "string",
  "external-identifier": "string",
  "cancellable": true,
  "value-effect": "string",
  "stop-trigger": "string",
  "cancelled-at": "2026-08-16T07:12:09.633Z",
  "leg-count": "string",
  "underlying-instrument-type": "string",
  "value": 0,
  "reject-reason": "string",
  "status": "string",
  "live-at": "2026-08-16T07:12:09.633Z",
  "preflight-id": "string",
  "price-effect": "string",
  "username": "string",
  "replacing-order-id": "string",
  "complex-order-id": "string",
  "order-type": "string",
  "source": "string",
  "id": "string",
  "order-rule": {
    "cancel-at": "2026-08-16T07:12:09.633Z",
    "cancelled-at": "2026-08-16T07:12:09.633Z",
    "route-after": "2026-08-16T07:12:09.633Z",
    "routed-at": "2026-08-16T07:12:09.633Z",
    "order-conditions": [
      {
        "instrument-type": "string",
        "triggered-at": "2026-08-16T07:12:09.633Z",
        "threshold": 0,
        "is-threshold-based-on-notional": true,
        "indicator": "string",
        "comparator": "string",
        "action": "string",
        "symbol": "string",
        "price-components": [
          {
            "instrument-type": "string",
            "quantity": "string",
            "quantity-direction": "string",
            "symbol": "string"
          }
        ],
        "triggered-value": 0,
        "id": "string"
      }
    ]
  },
  "user-id": "string",
  "cancelled-size": 0,
  "complex-order-tag": "string",
  "received-at": "2026-08-16T07:12:09.633Z"
}

PATCH
/accounts/{account_number}/orders/{id}
Edit price and execution properties of a live order by replacement. Subsequent fills of the original order
will abort the replacement.

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
patchAccountsAccountNumberOrdersId *
object
(body)
Example Value
Model
{
  "gtc-date": "2026-08-16",
  "order-type": "Limit",
  "stop-trigger": 0,
  "time-in-force": "Day",
  "price": 0,
  "price-effect": "Credit",
  "value": 0,
  "value-effect": "Credit",
  "automated-source": false,
  "external-identifier": "string",
  "partition-key": "string",
  "preflight-id": "string",
  "source": "string",
  "rules": {
    "cancel-at": "2026-08-16T07:11:55.991Z",
    "conditions": [
      {
        "action": "cancel",
        "instrument-type": "Bond",
        "symbol": "string",
        "comparator": "gte",
        "indicator": "last",
        "threshold": 0,
        "price-components": [
          {
            "instrument-type": "Bond",
            "quantity": 0,
            "quantity-direction": "Long",
            "symbol": "string"
          }
        ]
      }
    ],
    "route-after": "2026-08-16T07:11:55.991Z"
  }
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
200	
Edit price and execution properties of a live order by replacement. Subsequent fills of the original order
will abort the replacement.

Example Value
Model
{
  "size": "string",
  "global-request-id": "string",
  "time-in-force": "string",
  "terminal-at": "2026-08-16T07:12:09.640Z",
  "editable": true,
  "contingent-status": "string",
  "legs": [
    {
      "action": "string",
      "instrument-type": "string",
      "quantity": "string",
      "remaining-quantity": "string",
      "symbol": "string",
      "fills": [
        {
          "destination-venue": "string",
          "ext-exec-id": "string",
          "ext-group-fill-id": "string",
          "fill-id": "string",
          "fill-price": 0,
          "filled-at": "2026-08-16T07:12:09.640Z",
          "quantity": "string"
        }
      ]
    }
  ],
  "gtc-date": "2026-08-16",
  "updated-at": "string",
  "in-flight-at": "2026-08-16T07:12:09.640Z",
  "replaces-order-id": "string",
  "underlying-symbol": "string",
  "edited": true,
  "price": 0,
  "cancel-username": "string",
  "account-number": "string",
  "cancel-user-id": "string",
  "external-identifier": "string",
  "cancellable": true,
  "value-effect": "string",
  "stop-trigger": "string",
  "cancelled-at": "2026-08-16T07:12:09.640Z",
  "leg-count": "string",
  "underlying-instrument-type": "string",
  "value": 0,
  "reject-reason": "string",
  "status": "string",
  "live-at": "2026-08-16T07:12:09.640Z",
  "preflight-id": "string",
  "price-effect": "string",
  "username": "string",
  "replacing-order-id": "string",
  "complex-order-id": "string",
  "order-type": "string",
  "source": "string",
  "id": "string",
  "order-rule": {
    "cancel-at": "2026-08-16T07:12:09.640Z",
    "cancelled-at": "2026-08-16T07:12:09.640Z",
    "route-after": "2026-08-16T07:12:09.640Z",
    "routed-at": "2026-08-16T07:12:09.640Z",
    "order-conditions": [
      {
        "instrument-type": "string",
        "triggered-at": "2026-08-16T07:12:09.640Z",
        "threshold": 0,
        "is-threshold-based-on-notional": true,
        "indicator": "string",
        "comparator": "string",
        "action": "string",
        "symbol": "string",
        "price-components": [
          {
            "instrument-type": "string",
            "quantity": "string",
            "quantity-direction": "string",
            "symbol": "string"
          }
        ],
        "triggered-value": 0,
        "id": "string"
      }
    ]
  },
  "user-id": "string",
  "cancelled-size": 0,
  "complex-order-tag": "string",
  "received-at": "2026-08-16T07:12:09.640Z"
}

DELETE
/accounts/{account_number}/orders/{id}
Requests order cancellation

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
Responses
Response content type

application/json
Code	Description
200	
Requests order cancellation

Example Value
Model
{
  "size": "string",
  "global-request-id": "string",
  "time-in-force": "string",
  "terminal-at": "2026-08-16T07:12:09.645Z",
  "editable": true,
  "contingent-status": "string",
  "legs": [
    {
      "action": "string",
      "instrument-type": "string",
      "quantity": "string",
      "remaining-quantity": "string",
      "symbol": "string",
      "fills": [
        {
          "destination-venue": "string",
          "ext-exec-id": "string",
          "ext-group-fill-id": "string",
          "fill-id": "string",
          "fill-price": 0,
          "filled-at": "2026-08-16T07:12:09.645Z",
          "quantity": "string"
        }
      ]
    }
  ],
  "gtc-date": "2026-08-16",
  "updated-at": "string",
  "in-flight-at": "2026-08-16T07:12:09.645Z",
  "replaces-order-id": "string",
  "underlying-symbol": "string",
  "edited": true,
  "price": 0,
  "cancel-username": "string",
  "account-number": "string",
  "cancel-user-id": "string",
  "external-identifier": "string",
  "cancellable": true,
  "value-effect": "string",
  "stop-trigger": "string",
  "cancelled-at": "2026-08-16T07:12:09.645Z",
  "leg-count": "string",
  "underlying-instrument-type": "string",
  "value": 0,
  "reject-reason": "string",
  "status": "string",
  "live-at": "2026-08-16T07:12:09.645Z",
  "preflight-id": "string",
  "price-effect": "string",
  "username": "string",
  "replacing-order-id": "string",
  "complex-order-id": "string",
  "order-type": "string",
  "source": "string",
  "id": "string",
  "order-rule": {
    "cancel-at": "2026-08-16T07:12:09.645Z",
    "cancelled-at": "2026-08-16T07:12:09.645Z",
    "route-after": "2026-08-16T07:12:09.645Z",
    "routed-at": "2026-08-16T07:12:09.645Z",
    "order-conditions": [
      {
        "instrument-type": "string",
        "triggered-at": "2026-08-16T07:12:09.645Z",
        "threshold": 0,
        "is-threshold-based-on-notional": true,
        "indicator": "string",
        "comparator": "string",
        "action": "string",
        "symbol": "string",
        "price-components": [
          {
            "instrument-type": "string",
            "quantity": "string",
            "quantity-direction": "string",
            "symbol": "string"
          }
        ],
        "triggered-value": 0,
        "id": "string"
      }
    ]
  },
  "user-id": "string",
  "cancelled-size": 0,
  "complex-order-tag": "string",
  "received-at": "2026-08-16T07:12:09.645Z"
}

POST
/accounts/{account_number}/orders/{id}/dry-run
Runs through preflights for cancel-replace and edit without routing

Parameters
Name	Description
account_number *
integer($int32)
(path)
account_number
id *
integer($int32)
(path)
id
postAccountsAccountNumberOrdersIdDryRun *
object
(body)
Example Value
Model
{
  "gtc-date": "2026-08-16",
  "order-type": "Limit",
  "stop-trigger": 0,
  "time-in-force": "Day",
  "price": 0,
  "price-effect": "Credit",
  "value": 0,
  "value-effect": "Credit",
  "automated-source": false,
  "external-identifier": "string",
  "partition-key": "string",
  "preflight-id": "string",
  "source": "string",
  "legs": [
    {
      "action": "Allocate",
      "instrument-type": "Cryptocurrency",
      "quantity": 0,
      "symbol": "string"
    }
  ]
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Runs through preflights for cancel-replace and edit without routing

Example Value
Model
{
  "notes": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string",
      "url": "string"
    }
  ],
  "fee-calculation": "string",
  "order-costs": "string",
  "closing-fee-calculation": "string",
  "buying-power-effect": "string",
  "errors": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ],
  "order": {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.654Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.654Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.654Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.654Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.654Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.654Z",
      "cancelled-at": "2026-08-16T07:12:09.654Z",
      "route-after": "2026-08-16T07:12:09.654Z",
      "routed-at": "2026-08-16T07:12:09.654Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.654Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.654Z"
  },
  "complex-order": {
    "terminal-at": "string",
    "orders": [
      {
        "size": "string",
        "global-request-id": "string",
        "time-in-force": "string",
        "terminal-at": "2026-08-16T07:12:09.654Z",
        "editable": true,
        "contingent-status": "string",
        "legs": [
          {
            "action": "string",
            "instrument-type": "string",
            "quantity": "string",
            "remaining-quantity": "string",
            "symbol": "string",
            "fills": [
              {
                "destination-venue": "string",
                "ext-exec-id": "string",
                "ext-group-fill-id": "string",
                "fill-id": "string",
                "fill-price": 0,
                "filled-at": "2026-08-16T07:12:09.654Z",
                "quantity": "string"
              }
            ]
          }
        ],
        "gtc-date": "2026-08-16",
        "updated-at": "string",
        "in-flight-at": "2026-08-16T07:12:09.654Z",
        "replaces-order-id": "string",
        "underlying-symbol": "string",
        "edited": true,
        "price": 0,
        "cancel-username": "string",
        "account-number": "string",
        "cancel-user-id": "string",
        "external-identifier": "string",
        "cancellable": true,
        "value-effect": "string",
        "stop-trigger": "string",
        "cancelled-at": "2026-08-16T07:12:09.654Z",
        "leg-count": "string",
        "underlying-instrument-type": "string",
        "value": 0,
        "reject-reason": "string",
        "status": "string",
        "live-at": "2026-08-16T07:12:09.654Z",
        "preflight-id": "string",
        "price-effect": "string",
        "username": "string",
        "replacing-order-id": "string",
        "complex-order-id": "string",
        "order-type": "string",
        "source": "string",
        "id": "string",
        "order-rule": {
          "cancel-at": "2026-08-16T07:12:09.654Z",
          "cancelled-at": "2026-08-16T07:12:09.654Z",
          "route-after": "2026-08-16T07:12:09.654Z",
          "routed-at": "2026-08-16T07:12:09.654Z",
          "order-conditions": [
            {
              "instrument-type": "string",
              "triggered-at": "2026-08-16T07:12:09.654Z",
              "threshold": 0,
              "is-threshold-based-on-notional": true,
              "indicator": "string",
              "comparator": "string",
              "action": "string",
              "symbol": "string",
              "price-components": [
                {
                  "instrument-type": "string",
                  "quantity": "string",
                  "quantity-direction": "string",
                  "symbol": "string"
                }
              ],
              "triggered-value": 0,
              "id": "string"
            }
          ]
        },
        "user-id": "string",
        "cancelled-size": 0,
        "complex-order-tag": "string",
        "received-at": "2026-08-16T07:12:09.654Z"
      }
    ],
    "ratio-price-threshold": 0,
    "related-orders": [
      {
        "id": "string",
        "complex-order-id": "string",
        "complex-order-tag": "string",
        "replaces-order-id": "string",
        "replacing-order-id": "string",
        "status": "string"
      }
    ],
    "account-number": "string",
    "ratio-price-is-threshold-based-on-notional": true,
    "trigger-order": {
      "size": "string",
      "global-request-id": "string",
      "time-in-force": "string",
      "terminal-at": "2026-08-16T07:12:09.654Z",
      "editable": true,
      "contingent-status": "string",
      "legs": [
        {
          "action": "string",
          "instrument-type": "string",
          "quantity": "string",
          "remaining-quantity": "string",
          "symbol": "string",
          "fills": [
            {
              "destination-venue": "string",
              "ext-exec-id": "string",
              "ext-group-fill-id": "string",
              "fill-id": "string",
              "fill-price": 0,
              "filled-at": "2026-08-16T07:12:09.654Z",
              "quantity": "string"
            }
          ]
        }
      ],
      "gtc-date": "2026-08-16",
      "updated-at": "string",
      "in-flight-at": "2026-08-16T07:12:09.654Z",
      "replaces-order-id": "string",
      "underlying-symbol": "string",
      "edited": true,
      "price": 0,
      "cancel-username": "string",
      "account-number": "string",
      "cancel-user-id": "string",
      "external-identifier": "string",
      "cancellable": true,
      "value-effect": "string",
      "stop-trigger": "string",
      "cancelled-at": "2026-08-16T07:12:09.654Z",
      "leg-count": "string",
      "underlying-instrument-type": "string",
      "value": 0,
      "reject-reason": "string",
      "status": "string",
      "live-at": "2026-08-16T07:12:09.654Z",
      "preflight-id": "string",
      "price-effect": "string",
      "username": "string",
      "replacing-order-id": "string",
      "complex-order-id": "string",
      "order-type": "string",
      "source": "string",
      "id": "string",
      "order-rule": {
        "cancel-at": "2026-08-16T07:12:09.654Z",
        "cancelled-at": "2026-08-16T07:12:09.654Z",
        "route-after": "2026-08-16T07:12:09.654Z",
        "routed-at": "2026-08-16T07:12:09.654Z",
        "order-conditions": [
          {
            "instrument-type": "string",
            "triggered-at": "2026-08-16T07:12:09.654Z",
            "threshold": 0,
            "is-threshold-based-on-notional": true,
            "indicator": "string",
            "comparator": "string",
            "action": "string",
            "symbol": "string",
            "price-components": [
              {
                "instrument-type": "string",
                "quantity": "string",
                "quantity-direction": "string",
                "symbol": "string"
              }
            ],
            "triggered-value": 0,
            "id": "string"
          }
        ]
      },
      "user-id": "string",
      "cancelled-size": 0,
      "complex-order-tag": "string",
      "received-at": "2026-08-16T07:12:09.654Z"
    },
    "type": "string",
    "id": "string",
    "ratio-price-comparator": "string"
  },
  "warnings": [
    {
      "code": "string",
      "message": "string",
      "preflight-id": "string"
    }
  ]
}

GET
/customers/{customer_id}/orders
Returns a paginated list of the customer's orders (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Parameters
Name	Description
customer_id *
integer($int32)
(path)
customer_id
account-numbers *
array[string]
(query)
The account numbers to use for the lookup (example: account-numbers[]={value1}&account-numbers[]={value2})

page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 100

100
end-at
string($date-time)
(query)
DateTime end range for filtering orders in full date-time

end-at
end-date
string($date)
(query)
end-date
futures-symbol
string
(query)
Used to fetch both futures and futures options orders

futures-symbol
sort
string
(query)
The order to sort results in. Accepts Desc or Asc. Defaults to Desc

Available values : Desc, Asc

Default value : Desc


Desc
start-at
string($date-time)
(query)
DateTime start rage for filtering orders in full date-time. Required if start-date param not provided

start-at
start-date
string($date)
(query)
start-date
status
array[string]
(query)
(example: status[]={value1}&status[]={value2})

underlying-instrument-type
string
(query)
Underlying instrument type

underlying-instrument-type
underlying-symbol
string
(query)
underlying-symbol
Responses
Response content type

application/json
Code	Description
200	
Returns a paginated list of the customer's orders (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Example Value
Model
[
  {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.672Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.672Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.672Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.672Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.672Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.672Z",
      "cancelled-at": "2026-08-16T07:12:09.672Z",
      "route-after": "2026-08-16T07:12:09.672Z",
      "routed-at": "2026-08-16T07:12:09.672Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.672Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.672Z"
  }
]

GET
/customers/{customer_id}/orders/live
Returns a list of orders from the current trading day. Includes all statuses, not only Live.

Parameters
Name	Description
customer_id *
integer($int32)
(path)
customer_id
account-numbers *
array[string]
(query)
The account numbers to use for the lookup (example: account-numbers[]={value1}&account-numbers[]={value2})

page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 100

100
Responses
Response content type

application/json
Code	Description
200	
Returns a list of orders from the current trading day. Includes all statuses, not only Live.

Example Value
Model
[
  {
    "size": "string",
    "global-request-id": "string",
    "time-in-force": "string",
    "terminal-at": "2026-08-16T07:12:09.679Z",
    "editable": true,
    "contingent-status": "string",
    "legs": [
      {
        "action": "string",
        "instrument-type": "string",
        "quantity": "string",
        "remaining-quantity": "string",
        "symbol": "string",
        "fills": [
          {
            "destination-venue": "string",
            "ext-exec-id": "string",
            "ext-group-fill-id": "string",
            "fill-id": "string",
            "fill-price": 0,
            "filled-at": "2026-08-16T07:12:09.679Z",
            "quantity": "string"
          }
        ]
      }
    ],
    "gtc-date": "2026-08-16",
    "updated-at": "string",
    "in-flight-at": "2026-08-16T07:12:09.679Z",
    "replaces-order-id": "string",
    "underlying-symbol": "string",
    "edited": true,
    "price": 0,
    "cancel-username": "string",
    "account-number": "string",
    "cancel-user-id": "string",
    "external-identifier": "string",
    "cancellable": true,
    "value-effect": "string",
    "stop-trigger": "string",
    "cancelled-at": "2026-08-16T07:12:09.679Z",
    "leg-count": "string",
    "underlying-instrument-type": "string",
    "value": 0,
    "reject-reason": "string",
    "status": "string",
    "live-at": "2026-08-16T07:12:09.679Z",
    "preflight-id": "string",
    "price-effect": "string",
    "username": "string",
    "replacing-order-id": "string",
    "complex-order-id": "string",
    "order-type": "string",
    "source": "string",
    "id": "string",
    "order-rule": {
      "cancel-at": "2026-08-16T07:12:09.679Z",
      "cancelled-at": "2026-08-16T07:12:09.679Z",
      "route-after": "2026-08-16T07:12:09.679Z",
      "routed-at": "2026-08-16T07:12:09.679Z",
      "order-conditions": [
        {
          "instrument-type": "string",
          "triggered-at": "2026-08-16T07:12:09.679Z",
          "threshold": 0,
          "is-threshold-based-on-notional": true,
          "indicator": "string",
          "comparator": "string",
          "action": "string",
          "symbol": "string",
          "price-components": [
            {
              "instrument-type": "string",
              "quantity": "string",
              "quantity-direction": "string",
              "symbol": "string"
            }
          ],
          "triggered-value": 0,
          "id": "string"
        }
      ]
    },
    "user-id": "string",
    "cancelled-size": 0,
    "complex-order-tag": "string",
    "received-at": "2026-08-16T07:12:09.679Z"
  }
]

Models
ComplexOrder{
description:	
ComplexOrder model

id	string
account-number	string
ratio-price-comparator	string
ratio-price-is-threshold-based-on-notional	boolean
ratio-price-threshold	number($double)
terminal-at	string
type	string
related-orders	[...]
orders	[...]
trigger-order	{...}
}
postAccountsAccountNumberComplexOrders{
description:	
Creates a new Complex Order from supplied params

orders*	[...]
trigger-order	{...}
type*	string
The type of stragegy for the complex order i.e.
BLAST, OCO, OTO, OTOCO or PAIRS

Enum:
Array [ 5 ]
ratio-price-comparator*	string
How to compare against the ratio price.
Supports gte (Greater than or Equal To) or lte (Less than or Equal to)

Enum:
Array [ 2 ]
ratio-price-is-threshold-based-on-notional	boolean
If comparison is in notional value instead of price.

ratio-price-threshold*	number($double)
Ratio price for a PAIRS trade

source	string
The source the order is coming from

}
PlacedOrderResponse{
description:	
PlacedOrderResponse model

buying-power-effect	string
closing-fee-calculation	string
complex-order	{...}
errors	[...]
fee-calculation	string
notes	[...]
order	{...}
order-costs	string
warnings	[...]
}
postAccountsAccountNumberComplexOrdersDryRun{
description:	
Performs a dry-run for a new ComplexOrder from supplied params. Allows validation of potential orders.

orders*	[...]
trigger-order	{...}
type*	string
The type of stragegy for the complex order i.e.
BLAST, OCO, OTO, OTOCO or PAIRS

Enum:
Array [ 5 ]
ratio-price-comparator*	string
How to compare against the ratio price.
Supports gte (Greater than or Equal To) or lte (Less than or Equal to)

Enum:
Array [ 2 ]
ratio-price-is-threshold-based-on-notional	boolean
If comparison is in notional value instead of price.

ratio-price-threshold*	number($double)
Ratio price for a PAIRS trade

source	string
The source the order is coming from

}
patchAccountsAccountNumberComplexOrdersId{
description:	
Edit threshold-price of a PAIRS trade.

ratio-price-comparator	string
How to compare against the ratio price.
Supports gte (Greater than or Equal To) or lte (Less than or Equal to)

Enum:
Array [ 2 ]
ratio-price-threshold	number($double)
Ratio price for a PAIRS trade

}
postAccountsAccountNumberComplexOrdersIdDryRun{
description:	
Performs a dry-run for editing a ComplexOrder from supplied params.

ratio-price-comparator	string
How to compare against the ratio price.
Supports gte (Greater than or Equal To) or lte (Less than or Equal to)

Enum:
Array [ 2 ]
ratio-price-threshold	number($double)
Ratio price for a PAIRS trade

}
postAccountsAccountNumberOrders{
description:	
Accepts a json document containing parameters to create an order for the client.

gtc-date	string($date)
The date in which a GTD order will expire. Can only be provided if time-in-force is GTD.

order-type*	string
The type of order in regards to the price. i.e.
Limit, Market, Marketable Limit, Notional Market, Stop or Stop Limit

Enum:
Array [ 6 ]
stop-trigger*	number($double)
The price trigger at which a stop or stop-limit order becomes valid.

time-in-force*	string
The length in time before the order expires. i.e.
Day, Ext, Ext Overnight, GTC, GTC Ext, GTC Ext Overnight, GTD or IOC

Enum:
Array [ 8 ]
price	number($double)
The price of the Order. Required for limit and stop-limit orders.

price-effect*	string
If pay or receive payment for placing the order. i.e. Credit or Debit

Enum:
Array [ 2 ]
value	number($double)
The notional value of the Order, required for notional market orders.

value-effect*	string
If pay or receive payment for placing the notional market order. i.e. Credit or Debit

Enum:
Array [ 2 ]
automated-source	boolean
default: false
If the order was placed from an automated source

external-identifier	string
External identifier for the order

partition-key	string
Account partition key

preflight-id	string
Transient order identifier used for matching preflight errors to an individual order

source	string
The source the order is coming from

legs*	[...]
rules	{...}
advanced-instructions	{...}
}
postAccountsAccountNumberOrdersDryRun{
description:	
Accepts a json document containing parameters to create an order and
then runs the prefights without placing the order.

gtc-date	string($date)
The date in which a GTD order will expire. Can only be provided if time-in-force is GTD.

order-type*	string
The type of order in regards to the price. i.e.
Limit, Market, Marketable Limit, Notional Market, Stop or Stop Limit

Enum:
Array [ 6 ]
stop-trigger*	number($double)
The price trigger at which a stop or stop-limit order becomes valid.

time-in-force*	string
The length in time before the order expires. i.e.
Day, Ext, Ext Overnight, GTC, GTC Ext, GTC Ext Overnight, GTD or IOC

Enum:
Array [ 8 ]
price	number($double)
The price of the Order. Required for limit and stop-limit orders.

price-effect*	string
If pay or receive payment for placing the order. i.e. Credit or Debit

Enum:
Array [ 2 ]
value	number($double)
The notional value of the Order, required for notional market orders.

value-effect*	string
If pay or receive payment for placing the notional market order. i.e. Credit or Debit

Enum:
Array [ 2 ]
automated-source	boolean
default: false
If the order was placed from an automated source

external-identifier	string
External identifier for the order

partition-key	string
Account partition key

preflight-id	string
Transient order identifier used for matching preflight errors to an individual order

source	string
The source the order is coming from

legs*	[...]
rules	{...}
advanced-instructions	{...}
}
Order{
description:	
Order model

id	string
account-number	string
cancel-user-id	string
cancel-username	string
cancellable	boolean
cancelled-at	string($date-time)
cancelled-size	number($double)
complex-order-id	string
complex-order-tag	string
contingent-status	string
editable	boolean
edited	boolean
external-identifier	string
global-request-id	string
gtc-date	string($date)
in-flight-at	string($date-time)
leg-count	string
live-at	string($date-time)
order-type	string
preflight-id	string
price	number($double)
price-effect	string
received-at	string($date-time)
reject-reason	string
replaces-order-id	string
replacing-order-id	string
size	string
source	string
status	string
stop-trigger	string
terminal-at	string($date-time)
time-in-force	string
underlying-instrument-type	string
underlying-symbol	string
updated-at	string
user-id	string
username	string
value	number($double)
value-effect	string
legs	[...]
order-rule	{...}
}
patchAccountsAccountNumberOrdersId{
description:	
Edit price and execution properties of a live order by replacement. Subsequent fills of the original order
will abort the replacement.

gtc-date	string($date)
The date in which a GTD order will expire. Can only be provided if time-in-force is GTD.

order-type*	string
The type of order in regards to the price. i.e.
Limit, Market, Marketable Limit, Notional Market, Stop or Stop Limit

Enum:
Array [ 6 ]
stop-trigger*	number($double)
The price trigger at which a stop or stop-limit order becomes valid.

time-in-force*	string
The length in time before the order expires. i.e.
Day, Ext, Ext Overnight, GTC, GTC Ext, GTC Ext Overnight, GTD or IOC

Enum:
Array [ 8 ]
price	number($double)
The price of the Order. Required for limit and stop-limit orders.

price-effect*	string
If pay or receive payment for placing the order. i.e. Credit or Debit

Enum:
Array [ 2 ]
value	number($double)
The notional value of the Order, required for notional market orders.

value-effect*	string
If pay or receive payment for placing the notional market order. i.e. Credit or Debit

Enum:
Array [ 2 ]
automated-source	boolean
default: false
If the order was placed from an automated source

external-identifier	string
External identifier for the order

partition-key	string
Account partition key

preflight-id	string
Transient order identifier used for matching preflight errors to an individual order

source	string
The source the order is coming from

rules	{...}
}
putAccountsAccountNumberOrdersId{
description:	
Replaces a live order with a new one. Subsequent fills of the original order will abort the replacement.

gtc-date	string($date)
The date in which a GTD order will expire. Can only be provided if time-in-force is GTD.

order-type*	string
The type of order in regards to the price. i.e.
Limit, Market, Marketable Limit, Notional Market, Stop or Stop Limit

Enum:
Array [ 6 ]
stop-trigger*	number($double)
The price trigger at which a stop or stop-limit order becomes valid.

time-in-force*	string
The length in time before the order expires. i.e.
Day, Ext, Ext Overnight, GTC, GTC Ext, GTC Ext Overnight, GTD or IOC

Enum:
Array [ 8 ]
price	number($double)
The price of the Order. Required for limit and stop-limit orders.

price-effect*	string
If pay or receive payment for placing the order. i.e. Credit or Debit

Enum:
Array [ 2 ]
value	number($double)
The notional value of the Order, required for notional market orders.

value-effect*	string
If pay or receive payment for placing the notional market order. i.e. Credit or Debit

Enum:
Array [ 2 ]
automated-source	boolean
default: false
If the order was placed from an automated source

external-identifier	string
External identifier for the order

partition-key	string
Account partition key

preflight-id	string
Transient order identifier used for matching preflight errors to an individual order

source	string
The source the order is coming from

rules	{...}
}
postAccountsAccountNumberOrdersIdDryRun{
description:	
Runs through preflights for cancel-replace and edit without routing

gtc-date	string($date)
The date in which a GTD order will expire. Can only be provided if time-in-force is GTD.

order-type*	string
The type of order in regards to the price. i.e.
Limit, Market, Marketable Limit, Notional Market, Stop or Stop Limit

Enum:
Array [ 6 ]
stop-trigger*	number($double)
The price trigger at which a stop or stop-limit order becomes valid.

time-in-force*	string
The length in time before the order expires. i.e.
Day, Ext, Ext Overnight, GTC, GTC Ext, GTC Ext Overnight, GTD or IOC

Enum:
Array [ 8 ]
price	number($double)
The price of the Order. Required for limit and stop-limit orders.

price-effect*	string
If pay or receive payment for placing the order. i.e. Credit or Debit

Enum:
Array [ 2 ]
value	number($double)
The notional value of the Order, required for notional market orders.

value-effect*	string
If pay or receive payment for placing the notional market order. i.e. Credit or Debit

Enum:
Array [ 2 ]
automated-source	boolean
default: false
If the order was placed from an automated source

external-identifier	string
External identifier for the order

partition-key	string
Account partition key

preflight-id	string
Transient order identifier used for matching preflight errors to an individual order

source	string
The source the order is coming from

legs	[...]
}

Quote Alerts
 1.23.2 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
quote-alerts
Operations about quote-alerts



GET
/quote-alerts
Get quote alerts for current user

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Get quote alerts for current user

Example Value
Model
{
  "instrument-type": "string",
  "operator": "string",
  "triggered-at": "2026-08-16T07:13:56.693Z",
  "threshold": "string",
  "threshold-numeric": 0,
  "expires-at": "string",
  "alert-external-id": "string",
  "dx-symbol": "string",
  "provider": "string",
  "completed-at": "2026-08-16T07:13:56.693Z",
  "dismissed-at": "2026-08-16T07:13:56.693Z",
  "created-at": "2026-08-16T07:13:56.693Z",
  "symbol": "string",
  "field": "string",
  "user-external-id": "string",
  "expired-at": "2026-08-16T07:13:56.693Z"
}

POST
/quote-alerts
Create quote alert for symbol

Parameters
Name	Description
postQuoteAlerts *
object
(body)
Example Value
Model
{
  "dx-symbol": "string",
  "expires-at": "string",
  "field": "Last",
  "instrument-type": "string",
  "operator": ">",
  "symbol": "string",
  "threshold": "string",
  "threshold-numeric": "string"
}
Parameter content type

application/json
Responses
Response content type

application/json
Code	Description
201	
Create quote alert for symbol

Example Value
Model
{
  "instrument-type": "string",
  "operator": "string",
  "triggered-at": "2026-08-16T07:13:56.696Z",
  "threshold": "string",
  "threshold-numeric": 0,
  "expires-at": "string",
  "alert-external-id": "string",
  "dx-symbol": "string",
  "provider": "string",
  "completed-at": "2026-08-16T07:13:56.696Z",
  "dismissed-at": "2026-08-16T07:13:56.696Z",
  "created-at": "2026-08-16T07:13:56.696Z",
  "symbol": "string",
  "field": "string",
  "user-external-id": "string",
  "expired-at": "2026-08-16T07:13:56.696Z"
}

DELETE
/quote-alerts/{alert_external_id}
Cancel a quote alert for current user

Parameters
Name	Description
alert_external_id *
integer($int32)
(path)
alert_external_id
Responses
Response content type

application/json
Code	Description
204	
Cancel a quote alert for current user


Models
postQuoteAlerts{
description:	
Create quote alert for symbol

dx-symbol	string
expires-at	string
field*	string
Enum:
Array [ 4 ]
instrument-type	string
operator*	string
Enum:
Array [ 2 ]
symbol*	string
threshold*	string
threshold-numeric	string
}
QuoteAlertDeserializer{
description:	
QuoteAlertDeserializer model

alert-external-id	string
completed-at	string($date-time)
created-at	string($date-time)
dismissed-at	string($date-time)
dx-symbol	string
expired-at	string($date-time)
expires-at	string
field	string
instrument-type	string
operator	string
provider	string
symbol	string
threshold	string
threshold-numeric	number($double)
triggered-at	string($date-time)
user-external-id	string
}


Risk Parameters
 3.65.1 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
accounts
Operations about accounts



GET
/accounts/{account_number}/margin-requirements/{underlying_symbol}/effective
Get effective margin requirements for account

Parameters
Name	Description
account_number *
string
(path)
account_number
underlying_symbol *
string
(path)
underlying_symbol
Responses
Response content type

application/json
Code	Description
200	
Get effective margin requirements for account

Example Value
Model
{
  "clearing-identifier": "string",
  "underlying-symbol": "string",
  "naked-option-floor": 0,
  "naked-option-minimum": 0,
  "short-equity-initial": 0,
  "long-equity-initial": 0,
  "is-deleted": true,
  "short-equity-maintenance": 0,
  "long-equity-maintenance": 0,
  "naked-option-standard": 0
}

GET
/accounts/{account_number}/position-limit
Get the position limit

Parameters
Name	Description
account_number *
string
(path)
account_number
Responses
Response content type

application/json
Code	Description
200	
Get the position limit

Example Value
Model
{
  "equity-order-size": 0,
  "event-contract-total-notional-position-value": 0,
  "account-number": "string",
  "future-order-size": 0,
  "future-option-order-size": 0,
  "equity-option-order-size": 0,
  "equity-option-position-size": 0,
  "future-option-position-size": 0,
  "underlying-opening-order-limit": 0,
  "event-contract-notional-order-limit": 0,
  "id": 0,
  "future-position-size": 0,
  "equity-position-size": 0
}
margin-requirements-public-configuration
Operations about margin-requirements-public-configurations



GET
/margin-requirements-public-configuration
Publicly accessible, read only margin configuration

Parameters
No parameters

Responses
Response content type

application/json
Code	Description
200	
Publicly accessible, read only margin configuration

Example Value
Model
{
  "risk-free-rate": 0
}
span
Operations about spans



GET
/span/rows
Get span rows for a specific date and exchange

Parameters
Name	Description
date *
string($date)
(query)
The date of the SPAN file

date
exchange *
string
(query)
Exchange source of the SPAN file

Available values : CME, CFE


CME
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 1000

1000
Responses
Response content type

application/json
Code	Description
200	
Get span rows for a specific date and exchange

Example Value
Model
[
  {
    "file-date": "2026-08-16",
    "row-index": 0,
    "exchange": "string",
    "row-data": "string"
  }
]

Models
PositionLimit{
description:	
PositionLimit model

id	integer($int32)
account-number	string
equity-option-order-size	integer($int32)
equity-order-size	integer($int32)
event-contract-notional-order-limit	integer($int32)
future-option-order-size	integer($int32)
future-order-size	integer($int32)
underlying-opening-order-limit	integer($int32)
equity-option-position-size	integer($int32)
equity-position-size	integer($int32)
event-contract-total-notional-position-value	integer($int32)
future-option-position-size	integer($int32)
future-position-size	integer($int32)
}
MarginRequirement{
description:	
MarginRequirement model

long-equity-initial	number($double)
long-equity-maintenance	number($double)
naked-option-floor	number($double)
naked-option-minimum	number($double)
naked-option-standard	number($double)
short-equity-initial	number($double)
short-equity-maintenance	number($double)
underlying-symbol	string
clearing-identifier	string
is-deleted	boolean
}
MarginRequirementsGlobalConfiguration{
description:	
MarginRequirementsGlobalConfiguration model

risk-free-rate	number($double)
}
Row{
description:	
Row model

file-date	string($date)
row-index	integer($int32)
exchange	string
row-data	string
}

Symbol Search
 1.0.0 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
default


GET
/symbols/search/{symbol}
Returns an array of symbol data.

Parameters
Name	Description
symbol *
string
(path)
Symbol or fragment of a symbol to search, i.e. AAP will return AAP and AAPL data

symbol
Responses
Response content type

application/json
Code	Description
200	
Returns an array of symbol data.

Example Value
Model
{
  "symbol": "string",
  "description": "string",
  "listed-market": "string",
  "price-increments": "string",
  "trading-hours": "string",
  "options": true,
  "instrument-type": "string"
}

Models
SymbolData{
symbol	string
Symbol

description	string
Company name

listed-market	string
Listed Market

price-increments	string
Price Increments

trading-hours	string
Trading Hours

options	boolean
Has listed options

instrument-type	string
Instrument Type

}

Transactions
 10.9.2 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
transactions
Operations about transactions



GET
/accounts/{account_number}/transactions
Returns a paginated list of the account's transactions (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Parameters
Name	Description
account_number *
string
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 250

250
currency
string
(query)
Currency

currency
sort
string
(query)
The order to sort results in. Defaults to Desc, Accepts Desc or Asc.

Available values : Desc, Asc

Default value : Desc


Desc
sub-type
array[string]
(query)
Filter based on transaction_sub_type (example: sub-type[]={value1}&sub-type[]={value2})

type
string
(query)
Filter based on transaction_type

type
types
array[string]
(query)
Allows filtering on multiple transaction_types (example: types[]={value1}&types[]={value2})

action
string
(query)
The action of the transaction. i.e. Allocate, Buy, Buy to Close, Buy to Open, Sell, Sell to Close or Sell to Open

action
end-date
string($date)
(query)
The end date of transactions to query. Defaults to now.

Default value : OrderedMap {}

{}
futures-symbol
string
(query)
The full TW Future Symbol /ESZ9 or
/NGZ19 if two year digit are appropriate

futures-symbol
instrument-type
string
(query)
The type of Instrument. i.e. Bond, Cryptocurrency, Currency Pair, Equity, Equity Offering, Equity Option, Event Contract, Fixed Income Security, Future, Future Option, Index, Liquidity Pool, Unknown or Warrant

instrument-type
partition-key
string
(query)
Account partition key

partition-key
start-date
string($date)
(query)
The start date of transactions to query.

start-date
symbol
string
(query)
The Stock Ticker Symbol AAPL, OCC Option Symbol AAPL 191004P00275000,
TW Future Symbol /ESZ9, or TW Future Option Symbol ./ESZ9 EW4U9 190927P2975

symbol
underlying-symbol
string
(query)
The Underlying Symbol. The Ticker Symbol FB or
TW Future Symbol with out date component /M6E or
the full TW Future Symbol /ESU9

underlying-symbol
end-at
string($date-time)
(query)
DateTime end range for filtering transactions in full date-time

end-at
start-at
string($date-time)
(query)
DateTime start range for filtering transactions in full date-time

start-at
Responses
Response content type

application/json
Code	Description
200	
Returns a paginated list of the account's transactions (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Example Value
Model
[
  {
    "other-charge-description": "string",
    "instrument-type": "string",
    "net-value": 0,
    "agency-price": 0,
    "proprietary-index-option-fees-effect": "string",
    "lots": {
      "id": "string",
      "executed-at": "2026-08-16T07:15:47.688Z",
      "price": 0,
      "quantity": 0,
      "quantity-direction": "string",
      "transaction-date": "2026-08-16",
      "transaction-id": 0
    },
    "regulatory-fees": 0,
    "exchange": "string",
    "clearing-fees": 0,
    "ext-global-order-number": 0,
    "ext-exchange-order-number": "string",
    "underlying-symbol": "string",
    "currency-conversion-fees": 0,
    "transaction-type": "string",
    "price": 0,
    "cost-basis-reconciliation-date": "2026-08-16",
    "account-number": "string",
    "principal-price": 0,
    "ext-exec-id": "string",
    "quantity": 0,
    "commission": 0,
    "ext-group-fill-id": "string",
    "value-effect": "string",
    "other-charge-effect": "string",
    "leg-count": 0,
    "exchange-affiliation-identifier": "string",
    "destination-venue": "string",
    "currency": "string",
    "other-charge": 0,
    "value": 0,
    "executed-at": "2026-08-16T07:15:47.688Z",
    "is-estimated-fee": true,
    "commission-effect": "string",
    "ext-group-id": "string",
    "currency-conversion-fees-effect": "string",
    "transaction-sub-type": "string",
    "action": "string",
    "reverses-id": 0,
    "exec-id": "string",
    "created-at": "2026-08-16T07:15:47.688Z",
    "symbol": "string",
    "clearing-fees-effect": "string",
    "order-id": 0,
    "id": 0,
    "regulatory-fees-effect": "string",
    "description": "string",
    "proprietary-index-option-fees": 0,
    "transaction-date": "2026-08-16",
    "net-value-effect": "string"
  }
]
accounts
Operations about accounts



Models
Transaction{
description:	
Transaction model

id	integer($int32)
account-number	string
action	string
agency-price	number($double)
clearing-fees	number($double)
clearing-fees-effect	string
commission	number($double)
commission-effect	string
lots	{...}
cost-basis-reconciliation-date	string($date)
created-at	string($date-time)
currency	string
currency-conversion-fees	number($double)
currency-conversion-fees-effect	string
description	string
destination-venue	string
exchange	string
exchange-affiliation-identifier	string
exec-id	string
executed-at	string($date-time)
ext-exchange-order-number	string
ext-exec-id	string
ext-global-order-number	integer($int32)
ext-group-fill-id	string
ext-group-id	string
instrument-type	string
is-estimated-fee	boolean
leg-count	integer($int32)
net-value	number($double)
net-value-effect	string
order-id	integer($int32)
other-charge	number($double)
other-charge-description	string
other-charge-effect	string
price	number($double)
principal-price	number($double)
proprietary-index-option-fees	number($double)
proprietary-index-option-fees-effect	string
quantity	number($double)
regulatory-fees	number($double)
regulatory-fees-effect	string
reverses-id	integer($int32)
symbol	string
transaction-date	string($date)
transaction-sub-type	string
transaction-type	string
underlying-symbol	string
value	number($double)
value-effect	string
}


Transactions
 10.9.2 
[ Base URL: api.tastyworks.com ]
Download Open-api Json
This API is currently unversioned, so you can leave the `Accept-Version` header blank.
Filter by tag
transactions
Operations about transactions



GET
/accounts/{account_number}/transactions
Returns a paginated list of the account's transactions (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Parameters
Name	Description
account_number *
string
(path)
account_number
page-offset
integer($int32)
(query)
Default value : 0

0
per-page
integer($int32)
(query)
Default value : 250

250
currency
string
(query)
Currency

currency
sort
string
(query)
The order to sort results in. Defaults to Desc, Accepts Desc or Asc.

Available values : Desc, Asc

Default value : Desc


Desc
sub-type
array[string]
(query)
Filter based on transaction_sub_type (example: sub-type[]={value1}&sub-type[]={value2})

type
string
(query)
Filter based on transaction_type

type
types
array[string]
(query)
Allows filtering on multiple transaction_types (example: types[]={value1}&types[]={value2})

action
string
(query)
The action of the transaction. i.e. Allocate, Buy, Buy to Close, Buy to Open, Sell, Sell to Close or Sell to Open

action
end-date
string($date)
(query)
The end date of transactions to query. Defaults to now.

Default value : OrderedMap {}

{}
futures-symbol
string
(query)
The full TW Future Symbol /ESZ9 or
/NGZ19 if two year digit are appropriate

futures-symbol
instrument-type
string
(query)
The type of Instrument. i.e. Bond, Cryptocurrency, Currency Pair, Equity, Equity Offering, Equity Option, Event Contract, Fixed Income Security, Future, Future Option, Index, Liquidity Pool, Unknown or Warrant

instrument-type
partition-key
string
(query)
Account partition key

partition-key
start-date
string($date)
(query)
The start date of transactions to query.

start-date
symbol
string
(query)
The Stock Ticker Symbol AAPL, OCC Option Symbol AAPL 191004P00275000,
TW Future Symbol /ESZ9, or TW Future Option Symbol ./ESZ9 EW4U9 190927P2975

symbol
underlying-symbol
string
(query)
The Underlying Symbol. The Ticker Symbol FB or
TW Future Symbol with out date component /M6E or
the full TW Future Symbol /ESU9

underlying-symbol
end-at
string($date-time)
(query)
DateTime end range for filtering transactions in full date-time

end-at
start-at
string($date-time)
(query)
DateTime start range for filtering transactions in full date-time

start-at
Responses
Response content type

application/json
Code	Description
200	
Returns a paginated list of the account's transactions (as identified by the provided authentication token)
based on sort param. If no sort is passed in, it defaults to descending order.

Example Value
Model
[
  {
    "other-charge-description": "string",
    "instrument-type": "string",
    "net-value": 0,
    "agency-price": 0,
    "proprietary-index-option-fees-effect": "string",
    "lots": {
      "id": "string",
      "executed-at": "2026-08-16T07:15:47.688Z",
      "price": 0,
      "quantity": 0,
      "quantity-direction": "string",
      "transaction-date": "2026-08-16",
      "transaction-id": 0
    },
    "regulatory-fees": 0,
    "exchange": "string",
    "clearing-fees": 0,
    "ext-global-order-number": 0,
    "ext-exchange-order-number": "string",
    "underlying-symbol": "string",
    "currency-conversion-fees": 0,
    "transaction-type": "string",
    "price": 0,
    "cost-basis-reconciliation-date": "2026-08-16",
    "account-number": "string",
    "principal-price": 0,
    "ext-exec-id": "string",
    "quantity": 0,
    "commission": 0,
    "ext-group-fill-id": "string",
    "value-effect": "string",
    "other-charge-effect": "string",
    "leg-count": 0,
    "exchange-affiliation-identifier": "string",
    "destination-venue": "string",
    "currency": "string",
    "other-charge": 0,
    "value": 0,
    "executed-at": "2026-08-16T07:15:47.688Z",
    "is-estimated-fee": true,
    "commission-effect": "string",
    "ext-group-id": "string",
    "currency-conversion-fees-effect": "string",
    "transaction-sub-type": "string",
    "action": "string",
    "reverses-id": 0,
    "exec-id": "string",
    "created-at": "2026-08-16T07:15:47.688Z",
    "symbol": "string",
    "clearing-fees-effect": "string",
    "order-id": 0,
    "id": 0,
    "regulatory-fees-effect": "string",
    "description": "string",
    "proprietary-index-option-fees": 0,
    "transaction-date": "2026-08-16",
    "net-value-effect": "string"
  }
]
accounts
Operations about accounts



Models
Transaction{
description:	
Transaction model

id	integer($int32)
account-number	string
action	string
agency-price	number($double)
clearing-fees	number($double)
clearing-fees-effect	string
commission	number($double)
commission-effect	string
lots	{...}
cost-basis-reconciliation-date	string($date)
created-at	string($date-time)
currency	string
currency-conversion-fees	number($double)
currency-conversion-fees-effect	string
description	string
destination-venue	string
exchange	string
exchange-affiliation-identifier	string
exec-id	string
executed-at	string($date-time)
ext-exchange-order-number	string
ext-exec-id	string
ext-global-order-number	integer($int32)
ext-group-fill-id	string
ext-group-id	string
instrument-type	string
is-estimated-fee	boolean
leg-count	integer($int32)
net-value	number($double)
net-value-effect	string
order-id	integer($int32)
other-charge	number($double)
other-charge-description	string
other-charge-effect	string
price	number($double)
principal-price	number($double)
proprietary-index-option-fees	number($double)
proprietary-index-option-fees-effect	string
quantity	number($double)
regulatory-fees	number($double)
regulatory-fees-effect	string
reverses-id	integer($int32)
symbol	string
transaction-date	string($date)
transaction-sub-type	string
transaction-type	string
underlying-symbol	string
value	number($double)
value-effect	string
}

---

## Api Guides

### Oauth2
OAuth2 is a widely trusted security standard which tastytrade uses to keep your accounts safe. Instead of sharing your password with every app or website you use, OAuth2 lets you sign in through a service you already trust, like tastytrade. This means your password stays with tastytrade and is never shared with other apps. The app only receives a temporary access token that is limited to specific permissions and revoked anytime you want. It's like giving someone a temporary keycard to one room in your building instead of handing over your master key – much safer and you stay in control.

Short-lived, scoped access tokens (15 minutes) are significantly more secure than long-lived, unscoped session tokens (24 hours). If a 15-minute token gets compromised, an attacker has a very small window to cause damage and can only access the specific resources that token was authorized for – like read-only access to your account.

In contrast, a 24-hour session token gives an attacker an entire day with potentially full access to everything in your account (unless you have 2FA enabled on your account, which we highly recommend you do).

For these reasons, all tastytrade API users must use Oauth2 access tokens when interacting with the tastytrade API. Below are instructions on generating your own Oauth application, grant, and access tokens. Please also read through the Auth Patterns guide to learn how to provide access tokens as bearer tokens in the Authorization header of each API request.

Please Note: User-Agent

All OAuth requests must also include a User-Agent header or they will be rejected. The format must be <product>/<version>. The product and version you choose does not matter as long as it is submitted in the proper format. Most people make it specific to the client they are using, like tastytrade-api-client/1.0 or my-custom-client/2.0.

1. Create an Oauth Application
Customers can create their own OAuth2 application on my.tastytrade.com by accessing the Manage tab > My Profile > API > OAuth Applications

Click + New OAuth client.
Client Name will be populated with your tastytrade username
You will then fill out the following items:
Redirect URI per environment requested
Multiple URIs can be sent
These must be full URIs. https://www.my-redirect-uri.com is valid but my-redirect.com is not.
Scopes requested
Valid values: read, trade, openid
Click Create
Upon creating your application you will be presented with your Client ID and Client Secret. These should be stored securely before clicking Finish Setup.

Please Note: Your client secret is a value that we will only show you one time. It is crucial to store it securely upon receipt from tastytrade as it cannot be shared again without creating new credentials. If you do need to create a new client secret, use the Regenerate button at the top of the page. This will invalidate any active grants you may have.



2. Generate a personal grant
Tastytrade makes it easy for you to generate an Oauth2 grant for your own personal application so that you don't have to go through the entire authorization flow.

To generate a grant, head to my.tastytrade.com by accessing the Manage tab > My Profile > API > OAuth Applications. This will display your personal application with a "Manage" button on the right. Click Manage, then click "Create Grant." This will show you your grant's refresh token, which you should use to generate or refresh an access token.



Please Note: Refresh tokens are long-lived and do not expire. If you lose your refresh token or if it becomes compromised, you should delete the corresponding grant on the Api page of my.tastytrade.com. You can always delete a grant and generate a new one. A new grant will have a new refresh token to use in generating an access token.

3. Generate an Access Token
You can make an access token request at this endpoint: POST https://api.tastyworks.com/oauth/token (Sandbox: https://api.cert.tastyworks.com/oauth/token). The following parameters are required:

grant_type - refresh_token
refresh_token
client_secret
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-6.

The response will include a new access_token that you need to use as a bearer token in the Authorization header of each request. For example: Authorization: "Bearer <access token>"

Access tokens are good for 15 minutes, after which you'll need to generate another one. Sending a request with an expired access token will result in an Http 401 response code.

Please Note: The client_secret is the secret shown to you after you created the oauth application in step 1. If you lose your client secret, you can generate a new one by going to my.tastytrade.com and navigating to the Manage tab > My Profile > API page. Find your application and click Manage > Settings. The settings page will let you generate a new client secret for your Oauth application. This will not change your client id.



Becoming a Trusted Third Party
Personal OAuth applications are designed with security as the top priority. Initially, these applications are restricted to your account only – you can test and develop your integration, but other tastytrade users cannot connect to it yet. This protection ensures that only verified, trustworthy applications can request access to customer accounts.

To enable your application for other users, you'll need to complete our trusted third party verification process. This review helps us maintain the highest security standards for our customers' financial data and ensures your application meets our requirements for data handling and user protection.

If you're interested in becoming a trusted third party and allowing other tastytrade users to securely connect to your application, please contact api.support@tastytrade.com. Once approved, users will be able to safely authorize your application through our secure OAuth flow, with full control over what permissions they grant.

The instructions below apply only to trusted third party Oauth applications.

User Authorization
tastytrade supports the authorization code grant OAuth2 flow. After tastytrade verifies and approves your application, you can direct users to authorize your application at https://my.tastytrade.com/auth.html (Sandbox: https://cert-my.staging-tasty.works/auth.html) with the following parameters:

client_id - required
redirect_uri - required
response_type - required
scope - optional
state - optional
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.1.

The screenshot below is what users will see when directed to my.tastytrade.com/auth.html:

Upon successful authorization, the page will redirect users back to the provided redirect_uri with the following query parameters:

code - authorization code
state
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.

Request an Access Token
Upon obtaining an authorization code, clients must request an access token by hitting tastytrade's token endpoint: POST https://api.tastyworks.com/oauth/token (Sandbox: https://api.cert.tastyworks.com/oauth/token). The following parameters are required:

grant_type - authorization_code
code - the authorization code sent to the redirect uri
client_id
client_secret
redirect_uri
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3.

The response will include the following data:

access_token - the token to be sent in each subsequent request in the Authorization header
refresh_token - never expires
token_type - Bearer
expires_in - The number of seconds the access token will be valid
id_token - only returned if using the openid scope
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-5.1.

Refresh an Access Token
You can make a refresh token request at this endpoint: POST https://api.tastyworks.com/oauth/token (Sandbox: https://api.cert.tastyworks.com/oauth/token). The following parameters are required:

grant_type - refresh_token
refresh_token
client_secret
See the OAuth2 spec for more information: https://datatracker.ietf.org/doc/html/rfc6749#section-6.

The response will include a new access_token.

Please Note: Refresh tokens are long-lived and do not expire. If you lose your refresh token or if it becomes compromised, please reach out to tastytrade API support immediately.

Two Factor Authentication - 2FA
For sensitive scopes, tastytrade requires customers to use two factor authentication (2FA) in order to grant an application access to their account. Sensitive scopes currently include:

read
trade
In order to enable 2FA, a customer must navigate to My Profile | Security and enable either Authenticator App or SMS Authentication.



During the ‘Authorize with tastytrade’ step, after entering your tastytrade account credentials and clicking ‘Login’ the customer will be prompted to enter their 2FA code either sent via SMS or on the Authenticator App they have set up.

## Order Submission
Submitting an Order
Generating the proper JSON to submit an order for the first time can seem like a daunting task. For some, the biggest challenge is understanding how to structure a JSON order. For others, the challenge may be learning the meaning of the various JSON order attributes.

This page is intended to clarify all the order attributes for you as well as explain the rules about how to structure a JSON order. This page only describes attributes for submitting an order. Other order attributes like status are populated after order submission and are not described here.

This pag also assumes you are familiar with what an order means in the tastytrade ecosystem. For a primer, you can head to the High-level Concepts: Orders section of our API Overview.

Example Order
To start, take a look at the example multi-leg equity option order below. This order has 2 legs: the first is a 197.5 call that we are buying and the second is a 200 call that we are selling. The strike price is found in the last 8 numeric characters of the symbol, where the first 5 digits are the whole number value (left of the decimal point) and the last 3 digits are the decimal value (right of the decimal point).

Looking at each leg's symbol, we can see that both options have an expiration date of 2023-08-18.

Both legs have "Open" in the action, meaning we are opening up new positions.

We have specified a limit price of $1.09 Debit, meaning we won't pay more than $1.09 for this order.

If you submitted this order and it filled, the result would be 2 new positions in your account: a long 2023-08-18 call option with a 197.5 strike and a short 2023-08-18 call option with a 200 strike.

Example Multi-leg Equity Option Order
{   // Order Attributes
    "time-in-force": "Day", // Order will expire when the market closes
    "order-type": "Limit", // Order includes a limit price
    "price": "1.09", // Don't pay more than $1.09 for this trade as a whole
    "price-effect": "Debit", // Account will be debited for this trade
    "legs": [
    {
        // Leg Attributes
        "action": "Buy to Open", // Opening a new long position
        "symbol": "AAPL  230818C00197500", // AAPL Call Option with 197.5 strike price, option expires 2023-08-18
        "quantity": 1, // 1 option contract
        "instrument-type": "Equity Option", // Equity Option instrument
    },
    {
        // Leg Attributes
        "action": "Sell to Open", // Opening a new short position
        "symbol": "AAPL  230818C00200000", // AAPL Call Option with 200 strike price, option expires 2023-08-18
        "quantity": 1, // 1 option contract
        "instrument-type": "Equity", // Equity Option instrument
    }
  ]
}
The above order JSON might seem complicated, so let's simplify it a little by breaking the order JSON into 2 parts:

The Order attributes
The Leg attributes
Order Attributes
Order attributes are any attributes that apply to the order as a whole. In short, they are any JSON attributes outside the objects in the legs array.

Refer to the table below for the JSON attributes of an order object. Each attribute is explained in further detail in the sections below.

Order Attribute	Required	Type	Meaning
gtc-date	Only if time-in-force is GTD	Date string (yyyy-mm-dd)	The date at which a GTD order expires
legs	Yes	Array of leg objects	Refer to the Leg Attributes section below
order-type	Yes	String	The type of order being submitted
price	Yes unless order-type is Notional Market	Decimal	The limit price of the order
price-effect	Yes unless order-type is Notional Market	String	The price's direction in relation to your account (Credit or Debit)
source	No	String	Designates where the order originated. Value can be any string you want.
stop-trigger	Only if order-type is Stop or Stop Limit	String	The trigger price of a Stop or Stop Market order
time-in-force	Yes	String	How long the order will live if not filled or rejected
value	Only if order-type is Notional Market	Decimal	The dollar amount of an instrument to buy or sell
value-effect	Only if order-type is Notional Market	String	The direction of the value in relation to your account (Credit or Debit)
advanced-instructions	No	Json	Detailed instructions about the order
Order Type
Order type typically affects how the the price/value of an order is defined.

Refer to the table below for each order type and its meaning. Each order type is described in detail in the sections below as well.

Order Type	Meaning
Limit	Order must have a price and price-effect included in the JSON.
Market	Order must not have price or price-effect included in the JSON.
Stop	A market order with a stop-trigger price in the JSON.
Stop Limit	A limit order with a stop-trigger price in the JSON.
Notional Market	An order to buy a dollar amount of something rather than a specific quantity
Limit

Limit orders must have a price and price-effect, which is the min/max price you're willing to accept.

Here are the rules for the JSON structure of a Limit order:

Must include a price and price-effect
Here's an example: If you are buying shares of stock and submit a limit order with a price of $5, you're effectively saying "I won't pay more than $5 per share for 100 shares of AAPL." In a dream world some nice person would sell you those shares for $5 per share. In the real world your order would go live and probably remain live until it expires or you cancel it.

Limit Order Example
{
    "time-in-force": "Day",
    "order-type": "Limit",
    "price": 5,
    "price-effect": "Debit"
    "legs": [
    {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "quantity": 100,
        "action": "Buy to Open"
    }
  ]
}
Market

Market orders don't have a price at all. By submitting a market order, you are basically saying "I want this order filled as quick as possible and I'll accept any price."

Here are the rules for the JSON structure of a Market order:

Orders must only have 1 leg
Must not include a price or price-effect
time-in-force must not be GTC
Opening market orders cannot be submitted while the market is closed.

Please see our help center article on market orders for the pros and cons of market orders.

Market Order Example
{
    "time-in-force": "Day",
    "order-type": "Market",
    "legs": [
    {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "quantity": 100,
        "action": "Buy to Open"
    }
  ]
}
Stop

Stop orders are market orders that are triggered when the quote hits a specific price, called a stop-trigger. When the quote hits the stop-trigger, the order becomes a market order.

Here are the rules for the JSON structure of a Stop order:

Must not include a price or price-effect
Must include a stop-trigger price
time-in-force must not be GTC for opening orders
"Stop" and "Stop Market" are the same concept, but only a value of Stop is allowed in the JSON.

Please refer to our help center article on stop orders for more information about this order type.

Closing Stop Market Order Example
{
    "time-in-force": "GTC",
    "order-type": "Stop",
    "stop-trigger": 105,
    "legs": [
    {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "quantity": 100,
        "action": "Sell to Close"
    }
  ]
}
Stop Limit

Stop limit orders are limit orders that are triggered when the quote hits a specific price, called a stop-trigger. When the quote hits the stop-trigger, the order becomes a limit order.

Here are the rules for the JSON structure of a Stop Limit order:

Must include a price and price-effect
Must include a stop-trigger price
Please refer to our help center article on stop orders for more information about this order type.

Stop Limit Order Example
{
    "time-in-force": "GTC",
    "order-type": "Stop",
    "stop-trigger": 110,
    "price": 115,
    "price-effect": "Debit"
    "legs": [
    {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "quantity": 100,
        "action": "Buy to Open"
    }
  ]
}
Notional Market

Notional Market means you are buying a dollar amount of an instrument. For example, instead of buying 1 share of AAPL, you could buy $10 of AAPL.

NOTE - Notional orders can only be used to trade fractional shares. Please submit an order below ask price, or route a non-notional order for full shares.

Here are the rules for the JSON structure of a Notional Market order:

order-type must be Notional Market
Orders must only have 1 leg
Order legs cannot have quantity
Order must include value and value-effect
Only cryptocurrency and eligible equity symbols are permitted
Opening Notional Market orders cannot be submitted while the market is closed.

Notional Market orders must only have a single leg and the leg must not include a quantity.

The order must include a value, which is the dollar amount you wish to buy/sell. Along with value, you must also include value-effect. Refer to the value section below for more info.

Notional market orders are allowed for cryptocurrencies and eligible equities. To determine if an equity is eligible, refer to the is-fractional-quantity-eligible field of the equity instrument's JSON representation. If the value is false, your order will be rejected. See the equities section below for info on fetching an equity instrument.

Below is an example order JSON of buying $10 of AAPL:

Notional Market Order Example
{
    "time-in-force": "Day",
    "order-type": "Notional Market",
    "value": 10,
    "value-effect": "Debit",
    "legs": [
    {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "action": "Buy to Open"
    }
  ]
}
The amount of shares you receive depends on the underlying price. If AAPL stock were at $100 right now and you submitted the above order for $10 worth of stock, you'd receive approximately 0.1 shares.

Price and Price Effect
Price only applies to Limit and Stop Limit orders. Submitting a Market, Stop, or Notional Market order with a price is invalid and the request will be rejected.

When you include a price, you must always include a price-effect as well. Think of price effect as the direction the money is flowing in relation to your account. Debit means you are paying money. Credit means you are receiving money. In the example below, we are receiving $175.25 (Credit) to short sell 100 shares of AAPL stock.

Price effect is correlated to leg.action. If you are buying stock, your leg.action will be Buy to Open or Buy to Close. When buying, the price effect must be a Debit. Your order will be rejected if you try to buy for a credit. Same applies for selling. You can't sell something for a Debit.

Debit Limit Order Example
{
    "time-in-force": "Day",
    "order-type": "Limit",
    "price": 175.25,
    "price-effect": "Credit",
    "legs": [
    {
        "instrument-type": "Equity",
        "quantity": 100,
        "symbol": "AAPL",
        "action": "Sell to Open"
    }
  ]
}
Price Effect	Meaning
Credit	Price will be credited to the customer's account
Debit	Price will be debited from the customer's account
Time In Force
Time in force means "How long do I want this order to live before it expires?" Refer to the Market Sessions by Instrument Type section to see the hours for each session.

Time In Force	Meaning
Day	Works during regular market hours only. Expires at market close (3:00pm CT for equities, 4:00pm CT for futures).
GTC	Works during regular market hours and remains active until filled or manually canceled.
GTD	Works during regular market hours until filled or specified date. Requires gtc-date parameter.
Ext	Works during extended hours session (equities only: 6:00am - 7:00pm CT). Expires at 7:00pm CT.
GTC Ext	Works during extended hours session and remains active until filled or canceled.
Ext Overnight	Works during 24-hour session (equities only: 7:00pm - 7:00pm CT). Expires at 7:00pm CT.
GTC Ext Overnight	Works during 24-hour session and remains active until filled or canceled.
Day

Day orders (like the one above) will live until either the order fills or the market closes. If a day order does not get filled by the time the market closes, it transitions to expired.

GTC

Good 'til Canceled orders never expire. They will work until they are either filled or the customer cancels them.

GTD

Good 'til Date orders expire on a given date. If you submit a GTD order, you must also include a gtc-date in the JSON (Yes, calling it gtd-date would have made more sense - we apologize).

Please refer to our help center article about time in force for more information.

Ext

Extended hours orders expire at the end of the extended session. You may only submit single leg equity orders with this time in force.

GTC Ext

GTC Extended hours orders do not expire. They are active only during the extended session. You may only submit single leg equity orders with this time in force.

Ext Overnight

These orders are active during the 24-hour session (7pm-7pm CT). Expiration occurs every day at 7pm CT. If you submit an Ext Overnight order at noon, it will expire at 7pm the same day. If you submit an Ext Overnight order at 8:00pm CT, it will expire tomorrow at 7:00pm CT.

Markets are closed the night before a full holiday. For example, July 4th is a full day holiday. Markets will be closed starting July 3rd at 7:00pm CT. They will open up again July 4th at 7:00pm CT.

You may only submit single leg equity orders with this time in force.

GTC Ext Overnight

These orders are always active as long as the 24-hour session is open. They work until they are filled or until the user cancels the order.

You may only submit single leg equity orders with this time in force.

Market Sessions by Instrument Type
Equities and Equity Options
Regular: 8:30am-3:00pm CT
Extended: 6:00am-7:00pm CT
** 24-Hour**: 7:00pm-7:00pm CT. Session starts the night before a regular session. First session of the week starts 7:00pm CT Sunday night. Overnight market is closed the night before market holiday.
Futures
Regular: 5:00pm-4:00pm CT
Cryptocurrencies
Regular: 24/7 (continuous trading)
Value and Value Effect
This field only applies to Notional Market orders. It denotes the dollar amount you wish to buy/sell.

value must be accompanied by value-effect, which is identical to price-effect but only applies to Notional Market orders.

Including a price and a value is invalid and the request will be rejected.

Value Effect	Meaning
Credit	Value will be credited to the customer's account
Debit	Value will be debited from the customer's account
Leg Attributes
Leg attributes are specific to their order leg. Each order leg represents an actual instrument (like a stock or an option) with a quantity and a side (action).

All orders must have a minimum of 1 leg.

The maximum number of legs an order can have depends on the instrument type:

Equity, Futures, and Cryptocurrency orders are limited to 1 leg.

Equity Option and Future Option orders can have up to 4 legs.

Refer to the table below for the JSON attributes of a leg object. Each attribute is explained in further detail in the sections below.

Leg Attribute	Required	Type	Meaning
action	Yes	String	The side you are taking in relation to your positions
instrument-type	Yes	String	The class of the instrument being traded
quantity	Yes unless order-type is Notional Market	Decimal	The amount being bought or sold
symbol	Yes	String	The symbol representing the instrument being traded
Action
This is the "side" you are taking (buy or sell) combined with an "opening" or "closing" designation.

Action	Meaning
Buy to Open	Open a long position by buying the instrument. You must not have an existing short position in that instrument.
Sell to Open	Open a short position by selling the instrument. You must not have an existing long position in that instrument.
Buy to Close	Close a short position by buying the instrument. You must have an existing short position in that instrument.
Sell to Close	Close a long position by selling the instrument. You must have an existing long position in that instrument.
Buy	Only applies to single leg outright futures trades. This action allows you to buy an outright future regardless of your position. If you are short an outright, this will result in closing your position.
Sell	Only applies to single leg outright futures trades. This action allows you to sell an outright future regardless of your position. If you are long an outright, this will result in closing your position.
If you don't own any shares of AAPL and want to buy 100 shares, the action would be Buy to Open with a quantity of 100.

If you don't own any shares of AAPL and want to sell short 100 shares, the action would be Sell to Open with a quantity of 100.

If you own 100 shares of AAPL and want to close 10 shares, the action would be Sell to Close with a quantity of 10.

If you own 100 shares of AAPL and want to buy 100 more shares, the action would be Buy to Open with a quantity of 100.

Buy to Open and Sell to Open are opening actions, meaning they result in a new position being added to your account.

Buy to Close and Sell to Close are closing actions, meaning they result in an existing position's quantity decreasing.

You may send Buy or Sell for single leg outright futures trades if you wish. tastytrade's system will convert it to the appropriate action for you. Sending a Sell order when you are long an outright future contract will close your position. Sending a Sell order when you have no position will result in a new short position. Same logic applies for the Buy action with outright futures orders. You may still send the full action when trading futures if you wish to specify opening vs closing. See Advanced Instructions below for more info.

Advanced Instructions
You should only submit closing orders against existing positions. For convenience, tastytrade will correct the Open/Close for you when you submit a closing order but don't have a corresponding position. For example, if you submit a Buy to Close order for AAPL but you don't have an AAPL position to close, tastytrade will route the order as Buy to Open. Likewise submitting a Sell to Close order when you don't have a position to close will result in a Sell to Open order being routed.

If you wish to block this behavior, you should submit your order with advanced-instructions specifying that you want strict-position-effect-validation: true. For example:

Advanced Instructions Example
{
    "time-in-force": "Day",
    "order-type": "Limit",
    "price": 175.25,
    "price-effect": "Credit",
    "advanced-instructions": {
      "strict-position-effect-validation": true
    },
    "legs": [
    {
        "instrument-type": "Equity",
        "quantity": 100,
        "symbol": "META",
        "action": "Sell to Close"
    }
  ]
}
With strict-position-effect-validation, the order will be rejected if you don't have an existing position to close.

Instrument Type
Valid instrument types are:

Cryptocurrency
Equity
Equity Option
Future
Future Option
Quantity
This denotes how much you want to buy or sell. For cryptocurrency orders, the quantity can have a decimal value. For equities, equity options, futures, and future options, the quantity must be a whole number (integer).

Here are the rules for including quantity in the order leg JSON:

Must be a positive number
Can have a decimal value for cryptocurrencies, otherwise must be a whole number (integer)
Must not be included if order-type is Notional Market
If order-type is Notional Market, quantity must not be included in the JSON. A notional market order means you are buying a dollar amount of something, like "I want $10 of AAPL stock" or "I want $10 of BTC/USD". Instead, you must include a value in the order JSON, which is the dollar amount you wish to buy/sell.

Symbol
The symbol is the primary identifier of an instrument. Each leg must have its own distinct symbol with no overlap. An order with multiple legs with the same symbol is invalid and the request will be rejected.

If you are trading stocks, you'd just use the ticker symbol (AAPL for Apple, TSLA for Tesla, etc). Symbols for other instruments types like equity options and futures are a little less intuitive and will require you to look up the actual instrument symbols via our API.

Refer to the sections below to learn how to look up a symbol for each type of instrument.

Equity Options

If you want to trade options for a particular stock, the easiest thing to do is fetch the full option chain via the GET /option-chains/{stock_symbol}/nested endpoint found here. This endpoint returns all of the expirations that tastytrade offers for that given equity symbol. Nested inside each expiration is a list of strikes where each strike has a call and a put field, which are the call symbol and the put symbol, respectively. For an example, view the Instruments section of our Api Guide.

The nested endpoint is nice because it groups the options into their expirations and each expiration lists out the strike price with its associated call and put symbols in a very concise JSON format.

There are a couple other endpoints for fetching option chains: GET /option-chains/{stock_symbol}/compact lists out all option symbols for a given stock symbol without any expiration grouping. This endpoint might be useful if you already have a good idea of what you're looking to trade and just want a list of every option symbol for an underlying stock.

The last option chain endpoint is GET /option-chains/{stock_symbol}, which returns a list of equity option instruments for a given symbol. Each equity option is a full data representation, meaning it includes all data fields pertinent to that instrument, such as exercise-style, expiration-date, and expiration-type. As a result, this endpoint returns a lot of data. You would probably be better served by fetching data from the /option-chains/{stock_symbol}/nested endpoint, locating the symbols you're interested in trading, and then fetching only those instruments directly from the GET /instruments/equity-options endpoint.

Of course, fetching the instruments isn't required for submitting an order. You only need an instruments's symbol to trade. However, the equity option instrument JSON does provide additional data that many will find useful.

Equity Option orders can contain up to 4 legs.

Equities

Most of the time you won't need to look up any information for a particular stock. If you know the ticker symbol that you want to trade, you can just plug that into the symbol field of the leg JSON and submit your order.

If you want to view a full JSON representation of an equity instrument, you can either search multiple instruments by symbol or fetch a single isntrument by symbol:

To search for equities by symbol, send a GET /instruments/equities?symbol[]=AAPL&symbol[]=TSLA. View the docs for this endpoint here.

To fetch a single equity instrument by its symbol, you can hit the GET /instruments/equities/{symbol} endpoint directly.

The equity instrument JSON includes fields like is-index, listed-market, active, is-fractional-quantity-eligible, and is-closing-only.

Equity orders can contain only 1 leg.

Futures

To view all of the active futures contracts we offer, hit the GET /instruments/futures endpoint. Each item in the list returned is a full JSON representation of a future instrument. The JSON includes fields like symbol, expiration-date, expires-at, and last-trade-date. You can view the full JSON representation here.

If you are only interested in trading one or two specific futures products, you can filter the results by one or more product-code like this: GET /instruments/futures?product-code[]=CL&product-code[]=ES.

To get a list of all of the future product codes tastytrade supports, hit GET /instruments/future-products. This endpoint returns a JSON representation of a future product, not its instruments. You can't trade Crude Oil futures (/CL) the same way you trade stock. You must trade a specific futures contract, such as /CLU3, which is the September 2023 Crude Oil contract.

Futures orders can contain only 1 leg.

Future Options

The process of finding future options symbols is almost identical to the equity options process, but instead of hitting GET /option-chains/{stock_symbol}/nested, you hit GET /futures-option-chains/{product_code}/nested. The JSON returned is formatted a little differently than the equity options chains, but the overall idea is the same. Instruments are grouped into expirations, each of which contains a list of strikes with call and put fields, representing the call symbol and the put symbol, respectively.

There is also a GET /futures-option-chains/{product_code} endpoint which returns a list of all future option instruments for a given product code. This endpoint returns a lot of data. You would probably be better served by fetching data from the /futures-option-chains/{product_code}/nested endpoint and locating the symbols you're interested in trading. If you need more info on the symbols and their respective instruments, you should then fetch those instruments directly from the GET /instruments/futures-options?symbol[]=symbol1&symol[]=symbol2 endpoint documented here.

If you only want to trade a specific future option product, like monthly E-mini S&P 500 options (ES), you can filter your search by that option product code: GET /instruments/future-options?option-root-symbol=ES.

To get a list of all the future option product codes that tastytrade supports, hit GET /instruments/future-option-products. This endpoint returns a JSON representation of a future option product, not its instruments.

A future option product's JSON contains fields like root-symbol, code, exchange, and expiration-type. You should treat root-symbol as the primary identifier for a product, not code.

Future option orders can contain up to 4 legs.

Cryptocurrencies

You can retrieve a list of cryptocurrencies that tastytrade supports via the GET /instruments/cryptocurrencies endpoint documented here. Each instrument returned contains a symbol field that you should include in the leg JSON.

Cryptocurrency orders can only contain 1 leg.

Order Responses
Order Rejected
Our system runs a number of validations on any order it receives. Any validation errors will result in your order being rejected with a 422 status code and a json payload detailing the reason. For example:

"error": {
    "code": "preflight_check_failure",
    "message": "One or more preflight checks failed",
    "errors": [
        {
            "code": "cant_buy_for_credit",
            "message": "You cannot buy for a credit."
        }
    ]
}
The above response indicates that you tried to submit a buy order with a "price-effect": "Credit", which is not allowed.

Order Accepted
Assuming your order passes all the validations our order system performs, you will receive a response with an order id and a status of Routed, among other data. We'll provide a description of the response keys below:

buying-power-effect - A json object containing data that details the impact this order will have on your account's buying power.

closing-fee-calculation - An estimation of the fees you may see in the future when you close the position created by this order. This is provided as a convenience and is purely an estimation. The fees are not applied to this order.

fee-calculation - An estimation of the fees you may see as a result of this order being filled. Fees are broken into separate categories and totaled at the end.

order - A json object containing status and tracking details about your order. For an overview of order status flow, head to our Order Flow section. For an overview of tracking and managing your orders, head to our Order Management section.

order.legs.fills - An array of order leg fill details. An order leg can have zero or many fills. For example, if you have a buy order for 100 shares, you could potentially receive 100 fills (1 per share). You could also receive a single fill for 100 shares. Each fill will have a quantity and will affect your positions. For example, a buy order of 100 shares will result in a position with quantity 100 when the order fills. Head to our overview section on orders and positions for more details.

warnings - We return warnings when you do an order dry run via POST /accounts/{account_number}/orders/dry-run. These are informational warnings. Some may give you a heads up that your order will be rejected if you were to try to route it. Other warnings may indicate that the market is closed and your order will be routed when the market opens up again.

Example Order JSON Requests
The following are examples json bodies for POST /accounts/{account_number}/orders http requests. You may hit /accounts/{account_number}/orders/dry-run to validate the order without sending to any venue.

AAPL Market Order
{
    "time-in-force": "Day",
    "order-type": "Market",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "quantity": 1,
          "action": "Buy to Open"
      }
    ]
}
AAPL GTC Closing Order
{
    "time-in-force": "GTC",
    "price": 150.25,
    "price-effect": "Credit",
    "order-type": "Limit",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "quantity": 1,
          "action": "Sell to Close"
      }
    ]
}
Short Futures Limit Order
{
    "time-in-force": "Day",
    "price": 90.03,
    "price-effect": "Credit",
    "order-type": "Limit",
    "legs": [
      {
          "instrument-type": "Future",
          "symbol": "/CLZ2",
          "quantity": 1,
          "action": "Sell to Open"
      }
    ]
}
AAPL Bear Call Spread
{
    "time-in-force": "Day",
    "price": 0.85,
    "price-effect": "Credit",
    "order-type": "Limit",
    "legs": [
      {
          "instrument-type": "Equity Option",
          "symbol": "AAPL  221118C00155000",
          "quantity": 1,
          "action": "Sell to Open"
      },
      {
          "instrument-type": "Equity Option",
          "symbol": "AAPL  221118C00157500",
          "quantity": 1,
          "action": "Buy to Open"
      }
    ]
}
AAPL GTD Order
{
    "time-in-force": "GTD",
    "gtc-date": "2022-12-01",
    "price": 0.85,
    "price-effect": "Credit",
    "order-type": "Limit",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "quantity": 1,
          "action": "Buy to Open"
      }
    ]
}
Stop Limit Order
{
    "time-in-force": "Day",
    "price": 150.0,
    "price-effect": "Debit",
    "stop-trigger": 150.0,
    "order-type": "Limit",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "quantity": 1,
          "action": "Buy to Open"
      }
    ]
}
Iron Condor Order
{
  "source": "my-api-code",
  "order-type": "Limit",
  "time-in-force": "Day",
  "price": "1.51",
  "price-effect": "Credit",
  "legs": [
    {
      "instrument-type": "Equity Option",
      "symbol": "TSLA  230714P00210000",
      "action": "Buy to Open",
      "quantity": "1"
    },
    {
      "instrument-type": "Equity Option",
      "symbol": "TSLA  230714P00215000",
      "action": "Sell to Open",
      "quantity": "1"
    },
    {
      "instrument-type": "Equity Option",
      "symbol": "TSLA  230714C00282500",
      "action": "Sell to Open",
      "quantity": "1"
    },
    {
      "instrument-type": "Equity Option",
      "symbol": "TSLA  230714C00290000",
      "action": "Buy to Open",
      "quantity": "1"
    }
  ]
}
Notional Cryptocurrency Order
{
    "time-in-force": "GTC",
    "order-type": "Notional Market",
    "value": 10.0,
    "value-effect": "Debit",
    "legs": [
      {
          "instrument-type": "Cryptocurrency",
          "symbol": "BTC/USD",
          "action": "Buy to Open"
      }
    ]
}
Complex Orders
These orders are submitted to the POST /accounts/{account_number}/complex-orders endpoint.

Please refer to the help center article here for more information about these types of trades.

Please note that BLAST orders are deprecated and are not currently supported in any of our environments. Supported complex order types are: OTOCO, OCO, OTO, and PAIRS.

The following are some example JSON payloads for complex orders. Note that OTOCO orders have a trigger-order property as well as an orders property. The trigger order will go live immediately while the other orders will be in Contingent status until the trigger order fills. Once the trigger order fills, the other orders will be sent.

AAPL OTOCO Order
{
    "type": "OTOCO",
    "trigger-order": {
        "order-type": "Limit",
        "price": 157.97,
        "price-effect": "Debit",
        "time-in-force": "Day",
        "legs": [{
            "instrument-type": "Equity",
            "symbol": "AAPL",
            "action": "Buy to Open",
            "quantity": 100
        }]
    },
    "orders": [
        {
            "order-type": "Limit",
            "price": 198.68,
            "price-effect": "Credit",
            "time-in-force": "GTC",
            "legs": [{
                "symbol": "AAPL",
                "instrument-type": "Equity",
                "action": "Sell to Close",
                "quantity": 100
            }]
        },
        {
            "order-type": "Stop",
            "time-in-force": "GTC",
            "stop-trigger": 143.06,
            "legs": [{
                "symbol": "AAPL",
                "instrument-type": "Equity",
                "action": "Sell to Close",
                "quantity": 100
            }]
        }
    ]
}
OCO orders do not have a trigger-order property. The orders go live immediately.

AAPL OCO Order
{
    "type": "OCO",
    "orders": [
        {
            "order-type": "Limit",
            "price": 198.68,
            "price-effect": "Credit",
            "time-in-force": "GTC",
            "legs": [{
                "symbol": "AAPL",
                "instrument-type": "Equity",
                "action": "Sell to Close",
                "quantity": 100
            }]
        },
        {
            "order-type": "Stop",
            "time-in-force": "GTC",
            "stop-trigger": 143.06,
            "legs": [{
                "symbol": "AAPL",
                "instrument-type": "Equity",
                "action": "Sell to Close",
                "quantity": 100
            }]
        }
    ]
}
Fractional Stock Orders
tastytrade only supports fractional trading of certain equity products. To determine if an equity can be fractionally traded, fetch the equity instrument and check the is-fractional-quantity-eligible field. For example: GET /instruments/equities?symbol[]=AAPL returns "is-fractional-quantity-eligible": true.

Fractional orders must have a minimum monetary value of $5. Buy orders for 0.5 shares of a $1 stock will be rejected.

Fractional Quantity Order
{
    "time-in-force": "Day",
    "order-type": "Market",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "quantity": 0.5,
          "action": "Buy to Open"
      }
    ]
}
To buy $10 of AAPL stock, submit a Notional Market order with a value instead of a price. Omit the quantity field from the legs:

Notional Amount Order
{
    "time-in-force": "Day",
    "order-type": "Notional Market",
    "value": 10.0,
    "value-effect": "Debit",
    "legs": [
      {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "action": "Buy to Open"
      }
    ]
}


## Order Flow
Orders in the tastytrade system can go through many different status changes. These statuses can be hard to keep track of and can quickly become overwhelming for new traders. Don't get discouraged! This page is here to help you make sense of the order lifecycle and hopefully help you decipher the statuses of your various orders.

We find that it's easiest to group orders into 3 phases, each of which is described in more detail below:

Submission phase
Working phase
Terminal phase
Submission Phase
Order statuses in the submission phase: Received, Routed, Contingent, In Flight

This phase pertains to orders that have not yet been received by any exchange, and are therefore not considered "working" orders yet.

You may notice that when you initially submit your order, its status is often Routed. Routed means the order is currently being submitted to an exchange.

Immediately following Routed is the In Flight status. In Flight means the order has left our system and is awaiting confirmation that the exchange has received it.

You will see a Received status when you submit an order when the markets are closed. This means that we have received the order and it will be routed as soon as the markets open up.

A status of Contingent applies to complex orders, specifically OTOCO and OTO orders. These are special orders that sit dormant in our system until another order "triggers" them, at which point they will be routed to an exchange.

Replacement orders may also be Contingent immediately after you submit them. This means the replacement order is waiting to be "triggered" by the order it is replacing. The trigger occurs when the original order transitions to Canceled status, at which point the replacement order will be routed.

Occasionally you might submit an order and immediately get a Rejected status. This is considered a terminal status (see below) and can occur if, for example, your account doesn't have sufficient buying power to place the order.

Working Phase
Order statuses in the working phase: Live, Cancel Requested, Replace Requested

The working phase is when you can consider an order "live". In other words, it has made it to the exchange and the exchange has sent us confirmation. This phase can last anywhere from a few milliseconds to days or even months (GTC orders stay alive until you cancel them!)

The Live status is always the first status after the submission phase. Once an order is Live, you may cancel it or replace it.

Cancel Requested is the status when you decide to cancel your order. This means that we have sent your request to the exchange and are awaiting confirmation from them. Once we receive confirmation, the order status will become Canceled (see below).

Replace Requested works the same way. Suppose you have a Live order that you'd like to replace. We will refer to this Live order as the original order. You submit a replacement order and we mark the original order Replace Requested. We then send a cancel request for that original order to the exchange. During this time, the replacement order's status is Contingent, waiting for the original order to become Canceled. When the exchange confirms that it has canceled the original order, we will route the replacement order.

This highlights an important feature of the tastytrade system - we don't actually route your replacement order until your original order has been canceled. You can achieve the same effect by cancelling the original order and sending a new order separately. This just allows you to do it in one request.

Terminal Phase
Order statuses in the terminal phase: Filled, Canceled, Rejected, Expired

Once an order enters any of these statuses, it will receive no further status updates. It is terminal.

Filled means your order has been filled. If your order to buy to open 100 shares of AAPL was filled, you now own 100 shares of AAPL.

A few things happen when an order fills that we'd like to note here:

A position is created/updated. Fetch your positions here.
Your account balance is updated. Fetch your account balance here.
A trade transaction is created to reflect the trade and associated fees. Fetch your transactions here.
Note: tastytrade marks orders Filled as soon as possible, even if we haven't finished processing every single fill associated with the order. If your order's status is Filled but it missing fills for one or more legs, it means you hit our API while our system was still processing the fills. You should expect this behavior and can re-send your request to get the remaining fills after a brief delay. Fills should finish procesing within milliseconds.

Rejected status can occur during the submission phase or during the working phase. For example, an order for a non-existent stock symbol will be rejected without ever being routed.

Canceled means the user canceled the order.

Expired mostly applies to day orders that don't fill by the time the market closes. tastytrade automatically marks these as Expired.

Order Status Definitions
Status	Meaning	Terminal
Received	Initial order state	No
Routed	Order is on its way out of tastytrade's system	No
In Flight	Order is en route to the exchange	No
Live	Order is live at the exchange	No
Cancel Requested	Customer has requested to cancel the order. Awaiting a 'cancelled' message from the exchange.	No
Replace Requested	Customer has submitted a replacement order. This order is awaiting a 'cancelled' message from the exchange.	No
Contingent	This means the order is awaiting a status update of a related order. This pertains to replacement orders, complex OTOCO orders, and complex OTO orders.	No
Filled	Order has been fully filled	Yes
Cancelled	Order is cancelled	Yes
Expired	Order has expired. Usually applies to an option order.	Yes
Rejected	Order has been rejected by either tastytrade or the exchange.	Yes
Removed	Administrator has manually removed this order from customer account.	Yes
Partially Removed	Administrator has manually removed part of this order from customer account.	Yes
Examples of Order Status Transitions
1. Immediate fill

Suppose you submit a market order to buy a few shares of AMZN and it immediately gets filled. The order status would transition like this:

Received -> Routed -> In Flight -> Live -> Filled

2. Canceled by customer

Suppose you submit a limit order to buy some shares of AMZN but it doesn't fill right away, so you decide to cancel it:

Received -> Routed -> In Flight -> Live -> Cancel Requested -> Canceled

3. Expired day order

Suppose you submit a limit order to buy some shares of AMZN, give it a time-in-force of Day, but it sits at the exchange all day and doesn't fill. Finally the order expires when the markets close:

Received -> Routed -> In Flight -> Live -> Expired

4. Rejected by brokerage

Suppose you submit a limit order to buy some options that have already expired. tastytrade's system would recognize this and reject the order immediately. The order status would transition like this:

Received -> Rejected


## Order Management
Search Orders
Live Orders
Order Dry Run
Submit Order
Cancel Order
Cancel Replace
Complex Orders
Submit Complex Order
Cancel Complex Order
Search Orders
Returns a paginated list of orders filtered by the given parameters.

Query Parameters
start-date
Date
Date to start searching orders
example: 
2023-01-01
end-date
Date
Date to stop searching orders
example: 
2023-01-10
underlyng-symbol
String
Filter orders with this underlying symbol
example: 
AAPL
status
Array[String]
Filter orders with this status
example: 
status[]=Live&status[]=Filled
futures-symbol
String
Filter futures and future option orders with this outright contract symbol
example: 
ESM3
underlying-instrument-type
String
Filter orders with this underlying instrument type. "Future" will return both futures and future option orders. "Equity" will return both equities and equity option orders.
Values: 
Cryptocurrency, Equity, Future
sort
String
default: Desc
The chronological order that results are returned (ascending or descending)
Values: 
Asc, Desc
start-at
DateTime
Date and time to start searching orders
example: 
2023-01-01T00:00:00
end-at
DateTime
Date and time to stop searching orders
example: 
2023-01-05T01:00:00
per-page
Integer
default: 10
Number of paginated results to return at a time
page-offset
Integer
default: 0
Page number to fetch
Live Orders
Important: Do not poll the GET /orders/live endpoint for real-time order updates. Repeated polling of this endpoint degrades platform performance and may result in throttling or suspension of your API access.

Use the Account Streamer to receive order status updates in real time. The streamer delivers updates as they occur with lower latency and no risk of rate-limit issues.

Returns all orders that were created or updated today. The name is a little misleading. You'd think that it would only return orders with a status of "Live". That was our intention when we created this endpoint, however it has shifted slightly over time. In this endpoint's response, you'll find orders that were cancelled today, orders that were rejected today, GTC orders from months ago that are still Live today, orders that were filled today, etc.

GET
/accounts/{account_number}/orders/live
{
    "data": {
        "items": [
            {
                "id": 54758826,
                "account-number": "5WT00001",
                "time-in-force": "GTC",
                "order-type": "Limit",
                "size": 1,
                "underlying-symbol": "QQQ",
                "price": "3.0",
                "price-effect": "Debit",
                "status": "Live",
                "cancellable": true,
                "editable": true,
                "edited": false,
                "legs": [
                    {
                        "instrument-type": "Equity Option",
                        "symbol": "QQQ   191115C00187000",
                        "quantity": 1,
                        "remaining-quantity": 1,
                        "action": "Buy to Close",
                        "fills": []
                    }
                ]
            },
            {
                "id": 53959020,
                "account-number": "5WT00001",
                "time-in-force": "GTC",
                "order-type": "Limit",
                "size": 1,
                "underlying-symbol": "SPY",
                "price": "1.27",
                "price-effect": "Debit",
                "status": "Cancelled",
                "cancellable": true,
                "editable": true,
                "edited": false,
                "legs": [
                    {
                        "instrument-type": "Equity Option",
                        "symbol": "SPY   191018P00300000",
                        "quantity": 1,
                        "remaining-quantity": 1,
                        "action": "Buy to Close",
                        "fills": []
                    },
                    {
                        "instrument-type": "Equity Option",
                        "symbol": "SPY   191018P00270000",
                        "quantity": 1,
                        "remaining-quantity": 1,
                        "action": "Sell to Close",
                        "fills": []
                    }
                ]
            }
        ]
    },
    "api-version": "v1",
    "context": "/accounts/5WT05758/orders/live"
}
Order Dry Run
The dry run endpoint gives you a way to verify that an order will be accepted by our system without actually sending the order. This endpoint also returns a buying power impact and cost of the order (including estimated fees).

There are 2 main factors our system considers when performing a dry run:

The validity of the order
The state of your account
Order Validity

There are dozens of checks our system performs to ensure your order is valid. These include (but aren't limited to) things like:

Does this order have an invalid symbol?
Is this an order for an expired option?
Is the customer trying to close a position that doesn't exist?
Does the customer already have a working order that conflicts with this one?
State of Your Account

Your account must have sufficient buying power to afford the order. Your account must also be in good standing with tastytrade in order to place orders with us.

The dry run endpoint will return a warning in its response if it finds any issues with either your order or your account. If you try to actually submit the order after receiving a dry run warning, our system will reject the order and return an error message in the http response.

For more information on how to structure an order to submit, refer to our Order Submission guide.

POST
/accounts/{account_number}/orders/dry-run
{
    "data": {
        "order": {
            "account-number": "5WT0001",
            "time-in-force": "Day",
            "order-type": "Limit",
            "size": 1,
            "underlying-symbol": "SPY",
            "price": "2.0",
            "price-effect": "Credit",
            "status": "Received",
            "cancellable": true,
            "editable": true,
            "edited": false,
            "updated-at": 0,
            "legs": [
                {
                    "instrument-type": "Equity Option",
                    "symbol": "SPY   191018C00298000",
                    "quantity": 1,
                    "remaining-quantity": 1,
                    "action": "Buy to Open",
                    "fills": []
                },
                {
                    "instrument-type": "Equity Option",
                    "symbol": "SPY   191018C00295000",
                    "quantity": 1,
                    "remaining-quantity": 1,
                    "action": "Sell to Open",
                    "fills": []
                }
            ]
        },
        "warnings": [],
        "buying-power-effect": {
            "change-in-margin-requirement": "300.0",
            "change-in-margin-requirement-effect": "Debit",
            "change-in-buying-power": "102.302",
            "change-in-buying-power-effect": "Debit",
            "current-buying-power": "8995981.2613",
            "current-buying-power-effect": "Credit",
            "new-buying-power": "8995878.9593",
            "new-buying-power-effect": "Credit",
            "isolated-order-margin-requirement": "300.0",
            "isolated-order-margin-requirement-effect": "Debit",
            "is-spread": true,
            "impact": "102.302",
            "effect": "Debit"
        },
        "fee-calculation": {
            "regulatory-fees": "0.102",
            "regulatory-fees-effect": "Debit",
            "clearing-fees": "0.2",
            "clearing-fees-effect": "Debit",
            "commission": "2.0",
            "commission-effect": "Debit",
            "proprietary-index-option-fees": "0.0",
            "proprietary-index-option-fees-effect": "Debit",
            "total-fees": "2.302",
            "total-fees-effect": "Debit"
        }
    },
    "api-version": "v1",
    "context": "/accounts/5WT0001/orders/dry-run"
}
When you are ready to submit your order, you may post the exact same json to the submit order endpoint.

Submit Order
The requirements for this endpoint are identical to the dry-run endpoint, however this endpoint will actually send the order. The response will contain an id which you can use to look up or cancel the order.

For more information on how to structure an order to submit, refer to our Order Submission guide.

POST
/accounts/{account_number}/orders
{
    "data": {
        "order": {
            "id": 771043,
            "account-number": "5WT0001",
            "time-in-force": "Day",
            "order-type": "Limit",
            "size": 1,
            "underlying-symbol": "SPY",
            "price": "3.0",
            "price-effect": "Credit",
            "status": "Routed",
            "cancellable": false,
            "editable": false,
            "edited": false,
            "received-at": "2019-10-01T18:26:52.513+00:00",
            "updated-at": 1569954412572,
            "legs": [
                {
                    "instrument-type": "Equity Option",
                    "symbol": "SPY   191018C00295000",
                    "quantity": 1,
                    "remaining-quantity": 1,
                    "action": "Sell to Open",
                    "fills": []
                },
                {
                    "instrument-type": "Equity Option",
                    "symbol": "SPY   191018C00298000",
                    "quantity": 1,
                    "remaining-quantity": 1,
                    "action": "Buy to Open",
                    "fills": []
                }
            ]
        },
        "warnings": [],
        "buying-power-effect": {
            "change-in-margin-requirement": "300.0",
            "change-in-margin-requirement-effect": "Debit",
            "change-in-buying-power": "2.302",
            "change-in-buying-power-effect": "Debit",
            "current-buying-power": "8995871.0475",
            "current-buying-power-effect": "Credit",
            "new-buying-power": "8995868.7455",
            "new-buying-power-effect": "Credit",
            "isolated-order-margin-requirement": "300.0",
            "isolated-order-margin-requirement-effect": "Debit",
            "is-spread": true,
            "impact": "2.302",
            "effect": "Debit"
        },
        "fee-calculation": {
            "regulatory-fees": "0.102",
            "regulatory-fees-effect": "Debit",
            "clearing-fees": "0.2",
            "clearing-fees-effect": "Debit",
            "commission": "2.0",
            "commission-effect": "Debit",
            "proprietary-index-option-fees": "0.0",
            "proprietary-index-option-fees-effect": "Debit",
            "total-fees": "2.302",
            "total-fees-effect": "Debit"
        }
    },
    "api-version": "v1",
    "context": "/accounts/5WT0001/orders/"
}
Cancel Order
Requests cancellation of a given order. If the order is in a terminal status, this endpoint will return an HTTP 422 error with the following json:

{
    "error": {
        "code": "cannot_update_order",
        "message": "the order could not be cancelled"
    }
}
For more information on terminal order statuses, refer to our Order Flow guide.

For successful cancel requests, the order status will be Cancel Requested in the response. This means that our system is reaching out to the exchange to cancel your order. You should receive a notification via the Account Websocket when the order transitions to Cancelled status.

Path Parameters
account-number
String
The account number the order belongs to
id
Integer
The id of the order

DELETE
/accounts/{account_number}/orders/{id}
{
    "data": {
        "id": 12345,
        "account-number": "5WT00001",
        "time-in-force": "Day",
        "order-type": "Limit",
        "size": 1,
        "underlying-symbol": "AAPL",
        "underlying-instrument-type": "Equity",
        "price": "100.0",
        "price-effect": "Debit",
        "status": "Cancel Requested",
        "cancellable": false,
        "editable": false,
        "edited": false,
        "ext-exchange-order-number": "1234",
        "ext-client-order-id": "abcd1234",
        "ext-global-order-number": 9999,
        "received-at": "2023-07-31T15:33:45.899+00:00",
        "updated-at": 1690817636722,
        "legs": [
            {
                "instrument-type": "Equity",
                "symbol": "AAPL",
                "quantity": 1,
                "remaining-quantity": 1,
                "action": "Buy to Open",
                "fills": []
            }
        ]
    },
    "context": "/accounts/5WT00001/orders/12345"
}
Cancel Replace
When entering a cancel/replace order the following body parameters are able to be changed: price, order-type, and time-in-force. The rest of the json body must be the same as the original order being replaced.

PUT
/accounts/{account_number}/orders/{id}
{
  "time-in-force": "Day",
  "order-type": "Limit",
 "price": "3.0",
  "price-effect": "Credit",
  "legs": [
     {
     "instrument-type": "Equity Option",
      "symbol": "SPY   191018C00299000",
      "quantity": 1,
      "action": "Buy to Open"
     },
     {
     "instrument-type": "Equity Option",
      "symbol": "SPY   191018C00295000",
      "quantity": 1,
      "action": "Sell to Open"
    }
  ]
}
Complex Orders
Complex orders, commonly referred to as bracket orders, have separate endpoints for submission, retrieval, and cancellation.

The order JSON you submit is structured differently than normal orders. The structure depends on the type of complex order you are submitting.

To learn more about bracket orders and their purpose, refer to tastytrade's help center article here.

Submit Complex Order
OTOCO

OTOCO orders have one trigger-order and two orders in the JSON. The trigger order is an opening order that will "trigger" the 2 other orders if it fills.

The 2 other orders will sit dormant in Contingent status waiting for the trigger order to be filled. They are both closing orders, one being a "stop loss" order and the other being a "close at profit" order.

Example Request Body
{
  "type": "OTOCO",
  "trigger-order": {
    "time-in-force": "Day",
    "order-type": "Limit",
    "underlying-symbol": "UA",
    "price": 6.50,
    "price-effect": "Debit",
    "legs": [
      {
        "instrument-type": "Equity",
        "symbol": "UA",
        "quantity": 100,
        "action": "Buy to Open"
      }
    ]
  },
  "orders": [
    {
      "time-in-force": "Day",
      "order-type": "Limit",
      "underlying-symbol": "UA",
      "price": 8,
      "price-effect": "Credit",
      "legs": [
        {
          "instrument-type": "Equity",
          "symbol": "UA",
          "quantity": 100,
          "action": "Sell to Close"
        }
      ]
    },
    {
      "time-in-force": "Day",
      "order-type": "Stop",
      "underlying-symbol": "UA",
      "stop-trigger": 6,
      "legs": [
        {
          "instrument-type": "Equity",
          "symbol": "UA",
          "quantity": 100,
          "action": "Sell to Close"
        }
      ]
    }
  ]
}
The complex order response JSON contains an id for the overall complex order as well as 3 individual order ids (the trigger order id and the 2 closing order ids). The complex-order.id attribute can be used to fetch the complex order from the GET /accounts/{account_number}/complex-orders/{id} endpoint.

Each nested order id can be used to fetch the individual order via the GET /accounts/{account_number}/orders/{id} endpoint.

POST
/accounts/{account_number}/complex-orders Response
{
    "data": {
        "complex-order": {
            "id": 2000010530, // Use this to fetch entire complex order via GET /accounts/{account_number}/complex-orders/{id}
            "account-number": "5WT00001",
            "type": "OTOCO",
            "trigger-order": {
                "id": 2002750870,
                "account-number": "5WT00001",
                "time-in-force": "Day",
                "order-type": "Limit",
                "size": 100,
                "underlying-symbol": "UA",
                "underlying-instrument-type": "Equity",
                "price": "6.5",
                "price-effect": "Debit",
                "status": "Contingent",
                "contingent-status": "Pending Order",
                "cancellable": true,
                "editable": true,
                "edited": false,
                "received-at": "2023-07-31T16:13:56.326+00:00",
                "updated-at": 1690820036326,
                "complex-order-id": 2000010530,
                "complex-order-tag": "OTOCO::trigger-order",
                "preflight-id": 0,
                "legs": [
                    {
                        "instrument-type": "Equity",
                        "symbol": "UA",
                        "quantity": 100,
                        "remaining-quantity": 100,
                        "action": "Buy to Open",
                        "fills": []
                    }
                ]
            },
            "orders": [
                {
                    "id": 2002750871, // Use this to fetch individual order via GET /accounts/{account_number}/orders/{id}
                    "account-number": "5WT00001",
                    "time-in-force": "Day",
                    "order-type": "Limit",
                    "size": 100,
                    "underlying-symbol": "UA",
                    "underlying-instrument-type": "Equity",
                    "price": "8.0",
                    "price-effect": "Credit",
                    "status": "Contingent",
                    "contingent-status": "Pending Order",
                    "cancellable": true,
                    "editable": true,
                    "edited": false,
                    "received-at": "2023-07-31T16:13:56.356+00:00",
                    "updated-at": 1690820036356,
                    "complex-order-id": 2000010530,
                    "complex-order-tag": "OTOCO::oco-1-order",
                    "preflight-id": 1,
                    "legs": [
                        {
                            "instrument-type": "Equity",
                            "symbol": "UA",
                            "quantity": 100,
                            "remaining-quantity": 100,
                            "action": "Sell to Close",
                            "fills": []
                        }
                    ]
                },
                {
                    "id": 2002750872,
                    "account-number": "5WT00001",
                    "time-in-force": "Day",
                    "order-type": "Stop",
                    "size": 100,
                    "underlying-symbol": "UA",
                    "underlying-instrument-type": "Equity",
                    "stop-trigger": "6.0",
                    "status": "Contingent",
                    "contingent-status": "Pending Order",
                    "cancellable": true,
                    "editable": true,
                    "edited": false,
                    "received-at": "2023-07-31T16:13:56.381+00:00",
                    "updated-at": 1690820036381,
                    "complex-order-id": 2000010530,
                    "complex-order-tag": "OTOCO::oco-1-order",
                    "preflight-id": 2,
                    "legs": [
                        {
                            "instrument-type": "Equity",
                            "symbol": "UA",
                            "quantity": 100,
                            "remaining-quantity": 100,
                            "action": "Sell to Close",
                            "fills": []
                        }
                    ]
                }
            ]
        },
        "warnings": [],
        "buying-power-effect": {
            "change-in-margin-requirement": "325.0",
            "change-in-margin-requirement-effect": "Debit",
            "change-in-buying-power": "325.289",
            "change-in-buying-power-effect": "Debit",
            "current-buying-power": "5698377.62922645",
            "current-buying-power-effect": "Credit",
            "new-buying-power": "5698052.34022645",
            "new-buying-power-effect": "Credit",
            "isolated-order-margin-requirement": "325.0",
            "isolated-order-margin-requirement-effect": "Debit",
            "is-spread": false,
            "impact": "325.289",
            "effect": "Debit"
        },
        "fee-calculation": {
            "regulatory-fees": "0.0245",
            "regulatory-fees-effect": "Debit",
            "clearing-fees": "0.08",
            "clearing-fees-effect": "Debit",
            "commission": "0.0",
            "commission-effect": "None",
            "proprietary-index-option-fees": "0.0",
            "proprietary-index-option-fees-effect": "None",
            "total-fees": "0.1045",
            "total-fees-effect": "Debit"
        }
    },
    "context": "/accounts/5WT00001/complex-orders"
}
OTO

OTO orders have a trigger order and up to 3 additional orders that get routed when the trigger order fills. The additional orders do not cancel each other out the way an OTOCO's orders do.

For the example below, assume you have an iron condor position of 1 long AAPL Call, 1 short AAPL Call, 1 long AAPL Put, and 1 short AAPL Put. You'd like to close the short legs first, and then immediately trigger 2 market orders to close the long legs:

Example Request Body
{
  "type": "OTO",
  "trigger-order": {
    "time-in-force": "Day",
    "order-type": "Limit",
    "underlying-symbol": "AAPL",
    "price": 11.50,
    "price-effect": "Debit",
    "legs": [
      {
        "instrument-type": "Equity Option",
        "symbol": "AAPL  250214C00240000",
        "quantity": 1,
        "action": "Buy to Close"
      },
      {
        "instrument-type": "Equity Option",
        "symbol": "AAPL  250214P00235000",
        "quantity": 1,
        "action": "Buy to Close"
      }
    ]
  },
  "orders": [
    {
      "time-in-force": "Day",
      "order-type": "Market",
      "underlying-symbol": "AAPL",
      "legs": [
        {
          "instrument-type": "Equity Option",
          "symbol": "AAPL  250214C00242500",
          "quantity": 1,
          "action": "Sell to Close"
        }
      ]
    },
    {
      "time-in-force": "Day",
      "order-type": "Market",
      "underlying-symbol": "AAPL",
      "legs": [
        {
          "instrument-type": "Equity Option",
          "symbol": "AAPL  250214P00232500",
          "quantity": 1,
          "action": "Sell to Close"
        }
      ]
    }
  ]
}
POST
/accounts/{account_number}/complex-orders Response
{
    "data": {
        "buying-power-effect": {
            "change-in-margin-requirement": "250.0",
            "change-in-margin-requirement-effect": "Credit",
            "change-in-buying-power": "471.235745",
            "change-in-buying-power-effect": "Credit",
            "current-buying-power": "1176610.3696536",
            "current-buying-power-effect": "Credit",
            "new-buying-power": "1177081.6053986",
            "new-buying-power-effect": "Credit",
            "isolated-order-margin-requirement": "0.0",
            "isolated-order-margin-requirement-effect": "None",
            "is-spread": false,
            "impact": "471.235745",
            "effect": "Credit"
        },
        "complex-order": {
            "id": 2000054627,
            "account-number": "5WT05674",
            "type": "OTO",
            "trigger-order": {
                "id": 4007362691,
                "account-number": "5WT05674",
                "cancellable": true,
                "complex-order-id": 2000054627,
                "complex-order-tag": "OTO::trigger-order",
                "contingent-status": "Pending Order",
                "editable": true,
                "edited": false,
                "global-request-id": "e2905f39e30d832b45595dd752341642",
                "order-type": "Limit",
                "preflight-id": 0,
                "price": "1.5",
                "price-effect": "Debit",
                "received-at": "2025-01-28T15:43:09.626+00:00",
                "size": 1,
                "status": "Contingent",
                "time-in-force": "Day",
                "underlying-instrument-type": "Equity",
                "underlying-symbol": "AAPL",
                "updated-at": 1738078989626,
                "legs": [
                    {
                        "action": "Buy to Close",
                        "instrument-type": "Equity Option",
                        "quantity": 1,
                        "remaining-quantity": 1,
                        "symbol": "AAPL  250214C00240000",
                        "fills": []
                    },
                    {
                        "action": "Buy to Close",
                        "instrument-type": "Equity Option",
                        "quantity": 1,
                        "remaining-quantity": 1,
                        "symbol": "AAPL  250214P00235000",
                        "fills": []
                    }
                ]
            },
            "orders": [
                {
                    "id": 4007362692,
                    "account-number": "5WT05674",
                    "cancellable": true,
                    "complex-order-id": 2000054627,
                    "complex-order-tag": "OTO::order",
                    "contingent-status": "Pending Order",
                    "editable": true,
                    "edited": false,
                    "global-request-id": "e2905f39e30d832b45595dd752341642",
                    "order-type": "Market",
                    "preflight-id": 1,
                    "received-at": "2025-01-28T15:43:09.644+00:00",
                    "size": 1,
                    "status": "Contingent",
                    "time-in-force": "Day",
                    "underlying-instrument-type": "Equity",
                    "underlying-symbol": "AAPL",
                    "updated-at": 1738078989644,
                    "legs": [
                        {
                            "action": "Sell to Close",
                            "instrument-type": "Equity Option",
                            "quantity": 1,
                            "remaining-quantity": 1,
                            "symbol": "AAPL  250214C00242500",
                            "fills": []
                        }
                    ]
                },
                {
                    "id": 4007362693,
                    "account-number": "5WT05674",
                    "cancellable": true,
                    "complex-order-id": 2000054627,
                    "complex-order-tag": "OTO::order",
                    "contingent-status": "Pending Order",
                    "editable": true,
                    "edited": false,
                    "global-request-id": "e2905f39e30d832b45595dd752341642",
                    "order-type": "Market",
                    "preflight-id": 2,
                    "received-at": "2025-01-28T15:43:09.654+00:00",
                    "size": 1,
                    "status": "Contingent",
                    "time-in-force": "Day",
                    "underlying-instrument-type": "Equity",
                    "underlying-symbol": "AAPL",
                    "updated-at": 1738078989654,
                    "legs": [
                        {
                            "action": "Sell to Close",
                            "instrument-type": "Equity Option",
                            "quantity": 1,
                            "remaining-quantity": 1,
                            "symbol": "AAPL  250214P00232500",
                            "fills": []
                        }
                    ]
                }
            ]
        },
        "fee-calculation": {...},
        "warnings": []
    },
    "context": "/accounts/5WT05674/complex-orders"
}
OCO

OCO orders do not have a trigger-order. An OCO order is just a "stop loss" order and a "close at profit" order. Both are closing orders, meaning you must have an existing position to close in order to submit this complex order. When one order fills, the other order gets cancelled.

Example Request Body
{
    "type": "OCO",
    "orders": [{
    "order-type": "Limit",
    "price": 200.50,
    "price-effect": "Credit",
    "time-in-force": "GTC",
    "legs": [
        {
            "symbol": "AAPL",
            "instrument-type": "Equity",
            "action": "Sell to Close",
            "quantity": 100
            }
        ]
    },
    {
        "order-type": "Stop",
        "time-in-force": "GTC",
        "stop-trigger": 150.25,
        "legs": [{
            "symbol": "AAPL",
            "instrument-type": "Equity",
            "action": "Sell to Close",
            "quantity": 100
            }]
        }
    ]
}
POST
/accounts/{account_number}/complex-orders Response
{
    "data": {
        "complex-order": {
            "id": 2000010529,
            "account-number": "5WT00001",
            "type": "OCO",
            "orders": [
                {
                    "id": 2002750868,
                    "account-number": "5WT00001",
                    "time-in-force": "GTC",
                    "order-type": "Limit",
                    "size": 100,
                    "underlying-symbol": "AAPL",
                    "underlying-instrument-type": "Equity",
                    "price": "200.5",
                    "price-effect": "Credit",
                    "status": "Contingent",
                    "contingent-status": "Pending Order",
                    "cancellable": true,
                    "editable": true,
                    "edited": false,
                    "received-at": "2023-07-31T15:57:55.245+00:00",
                    "updated-at": 1690819075245,
                    "complex-order-id": 2000010529,
                    "complex-order-tag": "OCO::order",
                    "preflight-id": 0,
                    "legs": [
                        {
                            "instrument-type": "Equity",
                            "symbol": "AAPL",
                            "quantity": 100,
                            "remaining-quantity": 100,
                            "action": "Sell to Close",
                            "fills": []
                        }
                    ]
                },
                {
                    "id": 2002750869,
                    "account-number": "5WT00001",
                    "time-in-force": "GTC",
                    "order-type": "Stop",
                    "size": 100,
                    "underlying-symbol": "AAPL",
                    "underlying-instrument-type": "Equity",
                    "stop-trigger": "150.25",
                    "status": "Contingent",
                    "contingent-status": "Pending Order",
                    "cancellable": true,
                    "editable": true,
                    "edited": false,
                    "received-at": "2023-07-31T15:57:55.289+00:00",
                    "updated-at": 1690819075289,
                    "complex-order-id": 2000010529,
                    "complex-order-tag": "OCO::order",
                    "preflight-id": 1,
                    "legs": [
                        {
                            "instrument-type": "Equity",
                            "symbol": "AAPL",
                            "quantity": 100,
                            "remaining-quantity": 100,
                            "action": "Sell to Close",
                            "fills": []
                        }
                    ]
                }
            ]
        },
        "warnings": [],
        "buying-power-effect": {
            "change-in-margin-requirement": "4904.25",
            "change-in-margin-requirement-effect": "Credit",
            "change-in-buying-power": "311.741",
            "change-in-buying-power-effect": "Credit",
            "current-buying-power": "5698526.349726",
            "current-buying-power-effect": "Credit",
            "new-buying-power": "5698838.090726",
            "new-buying-power-effect": "Credit",
            "isolated-order-margin-requirement": "9808.5",
            "isolated-order-margin-requirement-effect": "Debit",
            "is-spread": false,
            "impact": "311.741",
            "effect": "Credit"
        },
        "fee-calculation": {
            "regulatory-fees": "0.1745",
            "regulatory-fees-effect": "Debit",
            "clearing-fees": "0.08",
            "clearing-fees-effect": "Debit",
            "commission": "0.0",
            "commission-effect": "None",
            "proprietary-index-option-fees": "0.0",
            "proprietary-index-option-fees-effect": "None",
            "total-fees": "0.2545",
            "total-fees-effect": "Debit"
        }
    },
    "context": "/accounts/5WT00001/complex-orders"
}
Path Parameters
account-number
String
The account submitting the order
Cancel Complex Order
Complex orders must be canceled through a separate endpoint where the id is the id of the complex order, not its trigger order or any of its nested orders.

Path Parameters
account-number
String
The account number the order belongs to
id
Integer
The id of the complex order. Not the trigger order id or any of the nested complex order ids.

DELETE
/accounts/{account_number}/complex-orders/{id}
{
    "data": {
        "id": 2000010530,
        "account-number": "5WT00001",
        "type": "OTOCO",
        "trigger-order": {
            "id": 2002750870,
            "account-number": "5WT00001",
            "time-in-force": "Day",
            "order-type": "Limit",
            "size": 100,

## Streaming Market Data
Get an Api Quote Token
DXLink Streamer
DXLink Symbology
DXLink Documentation
Candle Events (Historic Data)
Candle Events Examples
tastytrade supports streaming quote data asynchronously via websocket. If you want to fetch quote data synchronously, head to our Market Data Guide.

The process to subscribe to streaming market events has two parts.

Get an API quote token.
Using the api quote token, clients may fetch market events from DXLink (see DXLink Streamer section below).
Important: Api quote tokens expire after 24 hours

Please note that DxLink publishes market data events as they occur. Some tickers change rapidly due to high trading volume. Tickers with lower trading volume will see fewer market events. For tickers with extremely low liquidity, this could mean no market events for minutes or hours. This is normal. If you have a websocket that is connected and heartbeating but you aren't receiving any messages from DxLink, please check the ticker's liquidity. If nobody is trading that ticker, there won't be any events to publish.

Get an Api Quote Token
The GET /api-quote-tokens endpoint will return an api quote token and its associated urls.

This token is unique to the Customer identified by the access token on the request. It's used to identify the customer to TW's quote provider, DXLink.

GET
/api-quote-tokens
{
    "data": {
        "token": "<redacted>",
        "dxlink-url": <url>
        "level": "api"
    },
    "context": "/api-quote-tokens"
}
Quote streamer tokens are valid for 24 hours.

Please note that you must be registered as a tastytrade customer if you want to stream quotes. This means going through the account opening process. If you've only registered a username/password, your api quote token request will be rejected with the following error:

{'error':
  {'code': 'quote_streamer.customer_not_found_error',
  'message': 'You must be a customer to access a quote stream.'}
}
Head to tastytrade.com and click "Open An Account" to open your tastytrade account.

DXLink Streamer
We recommend using one of DxLinks SDKs to receive real-time market data events. Tastytrade has a typecript SDK that integrates the DxLink Javascript SDK and makes the following steps much simpler.

DXLink data is sent and received via a websocket connection. Use the dxlink-url provided to you in the /api-quote-tokens response to open a websocket.

All of the steps below are very well documented on DXLink's protocol documentation site. That same site also lets you use your api quote token to test out the DxLink protocol. We are providing some high level instructions below for convenience.

If you want to test in your browser, there is a chrome extension called Browser Websocket Client that you can install in your Google Chrome browser. We have a configuration file here that you can configure the client with. The file includes all of the example messages below.

It's important to send the messages to DxLink in the proper order. At a high level, the order is as follows:

SETUP
AUTHORIZE
CHANNEL_REQUEST - open a channel
FEED_SETUP - configure the channel
FEED_SUBSCRIPTION - subscribe to market events for one or more symbols
KEEPALIVE
Let's go into a little more detail on these. We'll provide example messages based on what you'll see when using the tastytrade web trading platform.

SETUP

This is the first message you send to initiate a connection to DXLink.

Sent: {"type":"SETUP","channel":0,"version":"0.1-DXF-JS/0.3.0","keepaliveTimeout":60,"acceptKeepaliveTimeout":60}
Received: {"type":"SETUP","channel":0,"keepaliveTimeout":60,"acceptKeepaliveTimeout":60,"version":"1.0-1.2.1-20240722-153442"}
AUTHORIZE

After SETUP, you should receive an AUTH_STATE message with state: UNAUTHORIZED. This is when you'd authorize with your api quote token. It's important that you wait until you receive the AUTHORIZED state response before continuing, otherwise DxLink will reject your subsequent messages:

Received: {"type":"AUTH_STATE","channel":0,"state":"UNAUTHORIZED"}
Sent: {"type":"AUTH","channel":0,"token":"<redacted>"}
Received: {"type":"AUTH_STATE","channel":0,"state":"AUTHORIZED","userId":"<redacted>"}
CHANNEL_REQUEST

You can then open up a channel on which to send subscription messages and receive market event data. A channel is a virtual connection that you may use to subscribe to different data. For example, you may want one channel for equities and another for futures. The channel number is any number you want to use to identify that channel.

Sent: {"type":"CHANNEL_REQUEST","channel":3,"service":"FEED","parameters":{"contract":"AUTO"}}
Received: {"type":"CHANNEL_OPENED","channel":3,"service":"FEED","parameters":{"contract":"AUTO","subFormat":"LIST"}}
FEED_SETUP

Once a channel is opened, you should configure what data fields to receive on that channel.

Sent: {"type":"FEED_SETUP","channel":3,"acceptAggregationPeriod":0.1,"acceptDataFormat":"COMPACT","acceptEventFields":{"Trade":["eventType","eventSymbol","price","dayVolume","size"],"TradeETH":["eventType","eventSymbol","price","dayVolume","size"],"Quote":["eventType","eventSymbol","bidPrice","askPrice","bidSize","askSize"],"Greeks":["eventType","eventSymbol","volatility","delta","gamma","theta","rho","vega"],"Profile":["eventType","eventSymbol","description","shortSaleRestriction","tradingStatus","statusReason","haltStartTime","haltEndTime","highLimitPrice","lowLimitPrice","high52WeekPrice","low52WeekPrice"],"Summary":["eventType","eventSymbol","openInterest","dayOpenPrice","dayHighPrice","dayLowPrice","prevDayClosePrice"]}}
Received: {"type":"FEED_CONFIG","channel":3,"dataFormat":"COMPACT","aggregationPeriod":0.1}
You can see from the above FEED_SETUP message that our web platform subscribes to a subset of data for each event type. This lets DxLink know to only send data like eventSymbol, price, dayVolume, and size when sending a Trade event.

FEED_SUBSCRIPTION

At this point you're ready to subscribe to market event data. DxLink will continue to stream these events to you over your channel until you unsubscribe to them. You can subscribe to multiple events for multiple symbols in a single message. We've abridged some of the message for readability.

Sent: {"type":"FEED_SUBSCRIPTION","channel":3,"reset":true,"add":[{"type":"Trade","symbol":"BTC/USD:CXTALP"},{"type":"Quote","symbol":"BTC/USD:CXTALP"},{"type":"Profile","symbol":"BTC/USD:CXTALP"},{"type":"Summary","symbol":"BTC/USD:CXTALP"},{"type":"Trade","symbol":"SPY"},{"type":"TradeETH","symbol":"SPY"},{"type":"Quote","symbol":"SPY"},{"type":"Profile","symbol":"SPY"},{"type":"Summary","symbol":"SPY"}]}
Received: {"type":"FEED_DATA","channel":3,"data":["Trade",["Trade","SPY",559.36,1.3743299E7,100.0,"Trade","BTC/USD:CXTALP",58356.71,"NaN","NaN"]]}
To stop receiving data for a symbol, you'd send another FEED_SUBSCRIPTION message with "remove" for each event type:

{"type":"FEED_SUBSCRIPTION","channel":3,"remove":[{"type":"Trade","symbol":"SPY"},{"type":"Quote","symbol":"SPY"},{"type":"Summary","symbol":"SPY"}]}
KEEPALIVE

You need to send a keepalive message at regular intervals to keep the websocket connection open. If DxLink doesn't receive a keepalive within the 60-second timeout, it will close the connection. Sending a keepalive message to them every 30 seconds will keep the connection alive indefinitely.

{"type":"KEEPALIVE","channel":0}
DxLink Market Data Events

DXLink provides several different market events, each of which has its own schema. For example, the Quote event provides data like bidPrice and askPrice while the Profile event provides data like high52WeekPrice and description. DXLink requires that you specify which market events you wish to subscribe to when you are adding a symbol subscription.

tastytrade's api quote token grants you access to the following market data events:

Profile
Quote
Summary
Trade
Greeks
TimeAndSale
For an overview of each of those events and the data they provice, head to DxLink's protocol docs which contains the schema for each event. For example, you can search the page for ProfileEvent or QuoteEvent and find all the data that comes in each of those events.

Symbology
To receive live market event data via DXLink, clients must convert symbols into a format that meets DxLink's requirements. For convenience, we provide these symbols via a field called streamer-symbol. You can find it in the http response body when fetching instrument data. For example, for subscribing to market events for a futures contract, you would hit the GET /instruments/futures endpoint:

GET
/instruments/futures
{
    "data": {
        "items": [
            {
                "symbol": "/6AM3",
                "streamer-exchange-code": "XCME",
                "streamer-symbol": "/6AM23:XCME"
            }
        ]
    }
}
An identical field is available for the following instruments endpoints:

GET /instruments/cryptocurrencies
GET /instruments/equities/:symbol
GET /instruments/futures
GET /futures-option-chains/:product-code/nested
GET /option-chains/:underlying-symbol/nested
DXLink Documentation
DXLink protocol documentation can be found here.

When setting up a feed with DxLink, be sure to use the COMPACT data format, as the FULL format uses a lot of data and will be turned off in the future. For example:

{
  "type": "FEED_SETUP",
  "channel": 1,
  "acceptAggregationPeriod": 10,
  "acceptDataFormat": "COMPACT",
  "acceptEventFields": {
    "Quote": ["eventType", "eventSymbol", "bidPrice", "askPrice", "bidSize", "askSize"]
  }
}
DXLink has an interactive debug console here. Use your api quote token to authenticate.

For convenience, DxFeed provides a number of SDKs for communicating with DxLink over websockets:

.NET API

Swift API

C/C++ API

Java API

JS API

Candle Events (Historic Data)
DxLink allows you to subscribe to different event types. One of these event types is Candle. A candle event represents quote data for a duration of time like 5 minutes, 1 hour, or 1 day. Each event has fields like open, close, high, and low.

When you subscribe to candle events for a specific symbol, you need to provide a period and a type. The type represents the unit of time with which each candle is measured, like minutes or hours. The period is a multiplier for the type, like five minutes or two hours.

You need both period and type to be present in the symbol when subscribing to candle events.

Please refer to DxFeed's guidelines for more information on how to generate a candle symbol correctly.

The final piece you need in order to subscribe to candle events is a fromTime timestamp. This is an integer in Unix epoch time format. For example, the timestamp representing August 8th, 2023 at 10:00am GMT is 1691402400. If today were August 9th, 2023 at 10:00am GMT and you used 1691402400 as the fromTime, you'd receive 24 hours of candle events.

Note: You can only set when to start getting candle data (fromTime). DxLink will give you all candles from that point until now. There's no way to set an end time - it always goes up to the current moment.

Here are a few use cases:

Suppose you wanted AAPL quote data from the past 24 hours and you wanted it grouped into 5-minute intervals. The fromTime would be now - 24 hours in epoch format. The symbol would look like AAPL{=5m} where 5 is the period and m is the type (minutes). Each candle event you receive will represent 5 minutes of data. The open field represents the price of AAPL at the start of the 5-minute candle duration and close represents the price of AAPL at the end of the 5-minute candle duration. high is the highest price that AAPL hit during those 5 minutes, and low is the lowest price that AAPL hit during those 5 minutes.

Suppose you wanted SPY quote data for the past 6 months grouped into 1-hour intervals. The symbol would look like SPY{=1h} and the fromTime would just be now - 6 months in epoch format.

Important We recommend using larger time intervals the further back you go. If you request too many candles you could get blasted with millions of events and bring your client to a crawl. For example, requesting 12 months of data grouped into 1-minute intervals would amount to around half a million events. That is a lot of data to process all at once.

Here is a rough guideline on what type and period to use based on how far back you reach:

Time Back	Recommended Type	Example	Notes
1 day	1 Minute	AAPL{=1m}	Returns around 1440 candle events
1 week	5 Minutes	AAPL{=5m}	Returns around 2016 candle events
1 month	30 Minutes	AAPL{=30m}	Returns around 1440 candle events
3 months	1 hour	AAPL{=1h}	Returns around 2160 candle events
6 months	2 hours	AAPL{=2h}	Returns around 2160 candle events
1 year+	1 day	AAPL{=1d}	Returns around 365 candle events
The last candle event you receive is always the "live" candle data. You may receive this event constantly as the quote changes and the candle data is updated for the current period. Specifically, the close value will update as the quote's price changes. For example, say you subscribed to AAPL{=5m} and it is currently 12:51. The "live" candle event should have a timestamp of 12:50. You should constantly receive messages for this event as the quote moves. Once the clock hits 12:55, the 12:50 event will close and the "live" event will be 12:55. This allows you to fetch whatever historica data you want and also be notified of the most recent quote statistics as they change.

Candle Events Examples
The following are code examples using tastytrade's typescript SDK. dxLinkFeed is an instance of DXLinkFeed from the DxLink javascript SDK. Refer to tastytrade's javascript SDK for instructions on setting that up.

5-minute candles for the past 24 hours
const date = new Date()
date.setDate(date.getDate() - 1) // Set date to 1 day ago
dxLinkFeed.addSubscriptions({ type: 'Candle', symbol: 'AAPL{=5m}', fromTime: date.getTime() })
30-minute candles for the past 30 days
const date = new Date()
date.setDate(date.getDate() - 30) // Set date to 30 days ago
dxLinkFeed.addSubscriptions({ type: 'Candle', symbol: 'AAPL{=30m}', fromTime: date.getTime() })
60-minute candles for the past 3 months
const date = new Date()
date.setDate(date.getDate() - 90) // Set date to 90 days ago
dxLinkFeed.addSubscriptions({ type: 'Candle', symbol: 'AAPL{=1h}', fromTime: date.getTime() })


## Streaming Account Data
Getting Started
Available Actions
Receiving Notifications
Notification Nuances
Hosts
Demo
The tastytrade API utilizes a websocket for publishing one-directional notifications from the API layer to trading client applications. We refer to this one-directional websocket as our "Account Streamer".

In general, we publish Account Streamer notifications about state changes to existing account data such as orders, balances, and positions. We also send notifications about state changes to non-account data such as public watchlists and quote alert triggers.

This allows clients to subscribe to real-time updates rather than polling API endpoints.

As an example, suppose you submit an opening order to buy 100 shares of AAPL. The HTTP response would look like this:

POST
/accounts/{account_number}/orders
{
    "data": {
        "order": {
            "id": 1,
            "account-number": "5WT00000",
            "time-in-force": "Day",
            "order-type": "Market",
            "size": 100,
            "underlying-symbol": "AAPL",
            "underlying-instrument-type": "Equity",
            "status": "Routed",
            "cancellable": true,
            "editable": true,
            "edited": false,
            "legs": [
                {
                    "instrument-type": "Equity",
                    "symbol": "AAPL",
                    "quantity": 100,
                    "remaining-quantity": 100,
                    "action": "Buy to Open",
                    "fills": []
                }
            ]
        }
    },
    "context": "/accounts/5WT00000/orders"
}
You'll notice the status is Routed, which means our system is in the process of sending your order to the exchange (for more info on order statuses and their many meanings, see the Order Flow page). Since this is a market order, it probably filled immediately. Let's re-fetch this order via the /GET /accounts/{account_number}/orders/{id} endpoint to verify:

GET
/accounts/{account_number}/orders/{id}
{
    "data": {
        "id": 1,
        "account-number": "5WT00000",
        "time-in-force": "Day",
        "order-type": "Market",
        "size": 100,
        "underlying-symbol": "AAPL",
        "underlying-instrument-type": "Equity",
        "status": "Filled",
        "cancellable": false,
        "editable": false,
        "edited": false,
        "ext-exchange-order-number": "12345",
        "ext-client-order-id": "67890",
        "ext-global-order-number": 1111,
        "received-at": "2023-07-05T19:07:32.444+00:00",
        "updated-at": 1688584052750,
        "in-flight-at": "2023-07-05T19:07:32.494+00:00",
        "live-at": "2023-07-05T19:07:32.495+00:00",
        "destination-venue": "TEST_A",
        "user-id": 99,
        "username": "coolperson",
        "terminal-at": "2023-07-05T19:07:32.737+00:00",
        "legs": [
            {
                "instrument-type": "Equity",
                "symbol": "AAPL",
                "quantity": 100,
                "remaining-quantity": 0,
                "action": "Buy to Open",
                "fills": [
                    {
                        "ext-group-fill-id": "0",
                        "ext-exec-id": "1122",
                        "fill-id": "24_TW::TEST_A47504::20230705.1179-TEST_FILL",
                        "quantity": 100,
                        "fill-price": "100.0",
                        "filled-at": "2023-07-05T19:07:32.496+00:00",
                        "destination-venue": "TEST_A"
                    }
                ]
            }
        ]
    },
    "context": "/accounts/5WT00000/orders/1"
}
The order is filled! But we had to make an additional HTTP request to fetch the order to check. What if you had an order that hadn't filled yet but instead was Live for a few minutes? Would you need to continuously be making requests to get its status? That seems like a pain! To alleviate this pain, you should utilize the account streamer.

The account streamer publishes real-time messages every time the order's status changes. All you need to do is open a websocket to the correct url (see hosts below) and subscribe to all account updates. As the order's status changes, you'll receive messages via the websocket that look like this:

Order Notification
{
  type: 'Order',
  data: {
    "id": 1,
    "account-number": "5WT00000",
    "time-in-force": "Day",
    "order-type": "Market",
    "size": 100,
    "underlying-symbol": "AAPL",
    "underlying-instrument-type": "Equity",
    "status": "Live",
    "cancellable": true,
    "editable": true,
    "edited": false,
    "legs": [
        {
            "instrument-type": "Equity",
            "symbol": "AAPL",
            "quantity": 100,
            "remaining-quantity": 100,
            "action": "Buy to Open",
            "fills": []
        }
    ]
  },
  timestamp: 1688595114405
}
Now you don't need to worry about polling or re-fetching your order!

Getting Started
At a high level, the following steps must be performed in order:

Open a websocket connection to the back-end host
Subscribe to notifications
Send heartbeats to back-end host
Note: If you don't perform these actions in the order listed above, you may get a not implemented error when attempting to subscribe to notifications. Please ensure you begin sending heartbeats after you have successfully sent a connect message.

1. Open a Websocket Connection
Open a websocket connection using your langauge of choice. For example, if you are running in a NodeJs environment, you could use the ws package:

Javascript Example
const WebSocket = require('ws')

const host = 'wss://streamer.cert.tastyworks.com'
const websocket = new WebSocket(host)
websocket.addEventListener('open', () => {
  // Schedule your heartbeat
})
websocket.addEventListener('message', (messageEvent) => {
  // Parse the message
})
You need to send an auth-token with every request made to the websocket server. This is your tastytrade access token. It's the same value you provide in the Authorization header of all other API requests. See our Auth Patterns section for more info.

NOTE: When using Access Tokens make sure you have 'Bearer ' preceding your token value within the auth-token field.

2. Subscribe to Notifications
There are several different types of notifications you can subscribe to. We refer to these types as "actions". You can think of it as subscribing to notifications regarding any actions that occur, such as quote changes, balance changes, watchlist updates, etc. For a complete list of the various actions, see Available Actions below.

Each subscribe message should contain an action, value, and auth-token key, like this:

Subscribe Message Schema
{
  "action": "<action>", // one of the available actions below
  "value": "<string>", // Optional. Depends on the message action being sent (see available actions below)
  "auth-token": "<string>" // access token
}
Most of the time you'll want to send a connect message, which subscribes you to account updates for whichever account numbers you provide:

Sample Connect Message
{
  "action": "connect",
  "value": ["5WT00000","5WT00001"],
  "auth-token": "your access token here",
  "request-id": 2
}
Connect Response Message
{
  "status": "ok",
  "action": "connect",
  "web-socket-session-id": "5b6e2799",
  "value": [ "5WT00000", "5WT00001" ],
  "request-id": 2
}
3. Send heartbeats
Once an active streamer connection has been established, you need to send heartbeat messages to the tastytrade server periodically (2s - 1m interval). This ensures that your websocket remains connected and also lets you detect streamer connection drops.

Sample Heartbeat Json
{
  "action": "heartbeat",
  "auth-token": "your access token here",
  "request-id": 1
}
The request-id isn't required, but our servers will include it in their response messages.

Heartbeat Response Message
{
  "status": "ok",
  "action": "heartbeat",
  "web-socket-session-id": "5b6e2799",
  "request-id": 1
}
Available Actions
heartbeat

Sent periodically to the streamer server to prevent the socket connection from being considered "stale"
When sending this message, value is blank
Heartbeat messages should be sent at regular intervals (2s-1m)
connect

Subscribes to all account-related notifications (orders, account balances, positions)
public-watchlists-subscribe

Although this subscribes to public watchlist updates, an auth token is still required
When sending this message, value is blank
quote-alerts-subscribe

Subscribes to quote alert messages about alerts the user has previously configured via a POST request to the /quote-alerts endpoint
When sending this message, value is blank
Important note: quote alerts exist at a user level, and not an account level
Receiving Notifications
All messages which are published via the streamer use the same json object representations as elsewhere in the API. Streamer messages always contain a full object representation, not a partial or a differential update.

Each message will have a type key that corresponds to the class of data being provided. For example, a notification with type: Order will have a full order json message in the data key:

Order Notification
{
  type: 'Order',
  data: {
    "id": 1,
    "account-number": "5WT00000",
    "time-in-force": "Day",
    "order-type": "Market",
    "size": 100,
    "underlying-symbol": "AAPL",
    "underlying-instrument-type": "Equity",
    "status": "Live",
    "cancellable": true,
    "editable": true,
    "edited": false,
    "legs": [
        {
            "instrument-type": "Equity",
            "symbol": "AAPL",
            "quantity": 100,
            "remaining-quantity": 100,
            "action": "Buy to Open",
            "fills": []
        }
    ]
  },
  timestamp: 1688595114405
}
Notification Nuances
Order Filled notifications
tastytrade marks an order Filled as soon as possible and sends fill data through the Account Streamer. Here’s how it works:

tastytrade gets fill data from the exchange for each leg of the order.
The order is marked Filled when there is no remaining quantity left to fill*.
tastytrade publishes fill data over the account streamer for each filled leg.
*For multi-leg option orders, which must be executed simultaneously, tastytrade marks the order Filled when the first leg is processed, assuming that leg has no remaining quantity. The first account streamer message will show the order as Filled with just one leg of fill data. The other fills will be processed and published over the account streamer immediately after. The final message will include all fill data for all legs.

We do this because order legs are often filled multiple times. For example, if you route an order to buy 100 shares of AAPL, you could receive 100 separate fills for 1 share each. We publish each of these fills as a separate message as they are processed.

Hosts
Sandbox : wss://streamer.cert.tastyworks.com

Production: wss://streamer.tastyworks.com

Demo
We have a demo page where you can subscribe to your sandbox account data. Head here to try it out.

## Sandbox Environment
The sandbox environment is a controlled system for open-api users.
The system resets every 24 hours, meaning all trades, transactions, and positions are deleted and balances are cleared out. Users, customers, and accounts are untouched.
To start, you will need to sign in with your sandbox user credentials. All subsequent actions here (customer creation, account creation) will be tied to this user.
The base url for all sandbox api endpoints is api.cert.tastyworks.com
The websocket url for account streamer updates is streamer.cert.tastyworks.com
Quotes in the sandbox environment are always 15-minutes delayed.
The base url for all sandbox api endpoints is api.cert.tastyworks.com.

The websocket url for account streamer updates is streamer.cert.tastyworks.com.

Orders in the sandbox environment all remain within the sandbox system. They never go to a real market.

Order statuses in the sandbox system are all determined by the order type and order price. The logic is as follows:

Market orders will always fill at a price of $1.
Limit orders with a price less than $3 will fill immediately.
Limit orders with a price greater than $3 will be Live and never fill.
Please note our Sandbox Environment instrumentation sometimes lags behind our live trading environment, which occasionally causes valid symbols to fail with 422 error codes. If you experience this please send a message to api.support@tastytrade.com and we will correct them right away.

Not every service is available in our sandbox environment. The following services are only available in our live trading system currently:

Net liquidating value history
Market metrics
Real-time market-data (streaming-market-data available via delayed feed)
OAuth2 in Sandbox

OAuth2 is supported in the Sandbox environment. Please use the tools at the bottom of the page to create and manage your personal application once you have logged in with your Sandbox user. If you lose your client secret you will need to regenerate it as it will only be shown once after creating the initial application.

Username or Email
Password
SIGN IN
Don't have a sandbox user? Register here
Forgot your password? Reset it here

## SDKs
Official SDKs
Below is a list of SDKs created by tastytrade

Javascript
Postman
We offer a public workspace containing all requests documented on this site.

tastytrade Api Workspace
Unofficial SDKs
Below is a list of SDKs created by the community

Python - tastyware/tastytrade
Ask AI
To assist LLMs with reading our API docs, we have a raw markdown version available for download

tastytrade LLM text files
