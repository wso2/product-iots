#!/bin/bash
# product-emm qsg sample setup script for copying the required files

echo "Copying the required files for wso2iots-3.0.0 QSG setup ..."
cp dropings/* ../../repository/components/dropins/ 
cp webapps/* ../../repository/deployment/server/webapps/ 


