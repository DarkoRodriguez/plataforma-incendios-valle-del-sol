#!/usr/bin/env python3
"""
Generate VAPID keys for Web Push notifications.
VAPID keys are required for the push notification system to work.

Usage:
    python3 generate_vapid_keys.py

This script will output the keys in Base64 URL-safe format (as required by the .env file).
"""

import sys

try:
    from cryptography.hazmat.primitives.asymmetric import ec
    from cryptography.hazmat.primitives import serialization
    from cryptography.hazmat.backends import default_backend
except ImportError:
    print("Error: cryptography library not found.")
    print("Install it with: pip install cryptography")
    sys.exit(1)

import base64

def generate_vapid_keys():
    """Generate EC VAPID keys for Web Push using PyJWT-compatible format."""
    
    # Generate EC key pair (P-256 / secp256r1)
    private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())
    public_key = private_key.public_key()
    
    # Get public key numbers
    public_numbers = public_key.public_numbers()
    private_numbers = private_key.private_numbers()
    
    # Convert to bytes (for P-256, each coordinate is 32 bytes)
    # Public key X and Y coordinates
    public_x = public_numbers.x.to_bytes(32, byteorder='big')
    public_y = public_numbers.y.to_bytes(32, byteorder='big')
    
    # Private key D value
    private_d = private_numbers.private_value.to_bytes(32, byteorder='big')
    
    # Uncompressed point for public key: 0x04 + X + Y (65 bytes total)
    public_key_raw = b'\x04' + public_x + public_y
    
    # Encode to base64 URL-safe (without padding)
    public_key_b64 = base64.urlsafe_b64encode(public_key_raw).decode('utf-8').rstrip('=')
    private_key_b64 = base64.urlsafe_b64encode(private_d).decode('utf-8').rstrip('=')
    
    return public_key_b64, private_key_b64

if __name__ == '__main__':
    print("Generating VAPID keys for Web Push notifications...\n")
    
    try:
        public_key, private_key = generate_vapid_keys()
        
        print("=" * 80)
        print("VAPID KEYS (Base64 URL-safe format)")
        print("=" * 80)
        print()
        print("Add these to your .env file:")
        print()
        print(f"VAPID_PUBLIC_KEY={public_key}")
        print(f"VAPID_PRIVATE_KEY={private_key}")
        print(f"VITE_VAPID_PUBLIC_KEY={public_key}")
        print()
        print("=" * 80)
        print()
        print("IMPORTANT:")
        print("- Keep the private key secret and never commit it to version control")
        print("- These keys must remain the same for existing subscriptions to work")
        print("- Set VAPID_SUBJECT to your email: VAPID_SUBJECT=mailto:your-email@example.com")
        print()
    except Exception as e:
        print(f"Error generating VAPID keys: {e}")
        sys.exit(1)
