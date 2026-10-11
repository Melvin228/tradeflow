-- Fixed illustrative prices for the simulated MVP; these are not live quotes.
INSERT INTO instrument (id, symbol, name, currency, current_price)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'AAPL', 'Apple Inc.', 'USD', 190.0000),
    ('10000000-0000-0000-0000-000000000002', 'MSFT', 'Microsoft Corporation', 'USD', 420.0000),
    ('10000000-0000-0000-0000-000000000003', 'AMZN', 'Amazon.com, Inc.', 'USD', 180.0000),
    ('10000000-0000-0000-0000-000000000004', 'GOOGL', 'Alphabet Inc.', 'USD', 175.0000),
    ('10000000-0000-0000-0000-000000000005', 'NVDA', 'NVIDIA Corporation', 'USD', 120.0000);
