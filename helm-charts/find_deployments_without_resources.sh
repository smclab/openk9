#!/bin/bash

echo "🔍 Cercando deployment.yaml senza sezione 'resources:' dichiarata..."
echo "=================================================================="

# Contatore per i deployment trovati
count=0

# Trova tutti i file deployment.yaml
find . -name "deployment.yaml" -type f | while read -r file; do
    # Controlla se il file contiene "resources:" 
    if ! grep -q "resources:" "$file"; then
        echo "❌ $file"
        ((count++))
    else
        echo "✅ $file (ha resources)"
    fi
done

echo "=================================================================="
echo "🏁 Ricerca completata!"

# Conta e mostra il totale dei deployment senza resources
total_without_resources=$(find . -name "deployment.yaml" -type f -exec grep -L "resources:" {} \;)
total_count=$(echo "$total_without_resources" | grep -c "deployment.yaml" 2>/dev/null || echo "0")

echo "📊 Totale deployment senza 'resources:': $total_count"

if [ "$total_count" -gt 0 ]; then
    echo ""
    echo "📋 Lista completa dei deployment senza resources:"
    echo "$total_without_resources"
fi 