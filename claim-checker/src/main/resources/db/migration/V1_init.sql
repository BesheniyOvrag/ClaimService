CREATE TABLE IF NOT EXISTS claims (
  id BIGSERIAL PRIMARY KEY,
  policy_type VARCHAR(20) NOT NULL CHECK (policy_type IN ('PREMIUM','STANDARD','ECONOMY')),
  name TEXT,
  surname TEXT,
  email TEXT NOT NULL,
  claim_date DATE NOT NULL,
  description TEXT,
  decision TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);
