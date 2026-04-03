import os

NEO4J_URI = os.getenv("NEO4J_URI", "bolt://localhost:7687")
NEO4J_USERNAME = os.getenv("NEO4J_USERNAME", "neo4j")
NEO4J_PASSWORD = os.getenv("NEO4J_PASSWORD", "AzusaNeo4j")
NEO4J_DATABASE = os.getenv("NEO4J_DATABASE", "neo4j")

Neo4j_Config = [
    NEO4J_URI,
    (NEO4J_USERNAME, NEO4J_PASSWORD),
    NEO4J_DATABASE
]