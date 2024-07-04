/*
 * Copyright 2024 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
use crate::prelude::*;
use clap::Parser;
use jsonwebtoken::Algorithm;
use reqwest::blocking::{Client, ClientBuilder};
use serde::{Deserialize, Serialize};
use serde_json::json;
use std::collections::HashMap;
use std::io::prelude::*;
use std::io::BufReader;
use std::ops::Add;
use std::path::PathBuf;

mod prelude;

#[derive(Parser)]
struct Opt {
    #[clap(env = "GOOGLE_APPLICATION_CREDENTIALS", long)]
    credentials: PathBuf,

    #[clap(env = "TOKEN", long)]
    token: String,
}

#[derive(Debug, Deserialize)]
struct GoogleApplicationCredentials {
    #[serde(rename = "type")]
    _type: String,

    project_id: String,

    private_key_id: String,

    private_key: String,

    client_email: String,

    client_id: String,

    auth_uri: String,

    token_uri: String,

    auth_provider_x509_cert_url: String,

    client_x509_cert_url: String,

    universe_domain: String,
}

/// https://firebase.google.com/docs/auth/admin/create-custom-tokens#create_custom_tokens_using_a_third-party_jwt_library
#[derive(Serialize)]
struct Claims {
    alg: String,
    iss: String,
    sub: String,
    aud: String,
    iat: u64,
    exp: u64,
    uid: String,
    scope: String,
}

impl Claims {
    fn new(
        cred: &GoogleApplicationCredentials,
        iat: std::time::SystemTime,
        exp: std::time::Duration,
        uid: String,
    ) -> Fallible<Self> {
        let iat = iat.duration_since(std::time::UNIX_EPOCH)?;
        // https://firebase.google.com/docs/auth/admin/create-custom-tokens#create_custom_tokens_using_a_third-party_jwt_library
        // https://github.com/googleapis/node-gtoken/blob/0fb58b3/src/index.ts#L316
        // https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages/send#authorization-scopes
        Ok(Self {
            alg: "RS256".into(),
            iss: cred.client_email.clone(),
            sub: cred.client_email.clone(),
            aud: cred.token_uri.clone(),
            iat: iat.as_secs(),
            exp: iat.add(exp).as_secs(),
            uid,
            scope: "https://www.googleapis.com/auth/firebase.messaging".into(),
        })
    }
}

#[derive(Debug, Deserialize)]
struct ClaimsResponse {
    access_token: String,
    expires_in: u16,
    token_type: String,
}

fn main() -> Fallible<()> {
    dotenv::dotenv().ok();

    tracing_subscriber::fmt::init();

    let opt = Opt::parse();

    let mut reader = BufReader::new(std::fs::File::open(opt.credentials)?);
    let mut buf = String::new();
    reader.read_to_string(&mut buf)?;

    let cred = serde_json::from_str::<GoogleApplicationCredentials>(&buf)?;
    info!(?cred);

    let jwt = jsonwebtoken::encode(
        &jsonwebtoken::Header::new(Algorithm::RS256),
        &Claims::new(
            &cred,
            std::time::SystemTime::now(),
            std::time::Duration::from_secs(60 * 60), // after an hour.
            "study-fcm".into(),
        )?,
        &jsonwebtoken::EncodingKey::from_rsa_pem(cred.private_key.as_bytes())?,
    )?;

    info!(?jwt);

    let client = ClientBuilder::new()
        .user_agent("com.study.fcm/0.1")
        .build()?;

    let res = client
        .post(&cred.token_uri)
        .form(&HashMap::from([
            // https://github.com/googleapis/node-gtoken/blob/0fb58b3/src/index.ts#L339
            ("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"),
            ("assertion", &jwt),
        ]))
        .send()?;
    let res_text = res.text()?;
    let res_obj = serde_json::from_str::<ClaimsResponse>(&res_text)?;

    info!(?res_obj);

    // https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages/send
    let res = client
        .post(&format!("https://fcm.googleapis.com/v1/projects/{}/messages:send", cred.project_id))
        .bearer_auth(&res_obj.access_token)
        .json(&json!({
            "validate_only": false,
            "message": {
                "data": {
                    "app.foo": std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH)?.as_secs().to_string(),
                },
                "android": {
                    "priority": "HIGH",
                },
                "token": &opt.token,
            },
        }))
        .send()?;
    let res_text = res.text()?;

    info!(%res_text);

    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use clap::CommandFactory;

    #[test]
    fn verify_cli() {
        Opt::command().debug_assert();
    }
}
