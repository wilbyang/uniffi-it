
### Build the Rust library
`cargo build --release`

### Generate Kotlin bindings using UniFFI in library mode
`cargo run --bin uniffi-bindgen generate --library target/release/libarithmetic.dylib --language kotlin --out-dir out`


