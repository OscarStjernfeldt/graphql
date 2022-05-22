# GraphQL (micro-service)

------
<details> <summary><b>Docker</b></summary>

### Docker image:

```
docker pull ghcr.io/oscarstjernfeldt/graphql:latest
```

```
docker run -p 8080:8080 --network <network> --name=graphql -e CONSUL_HOST=<consul> ghcr.io/oscarstjernfeldt/graphql:latest
```

</details>

------
<details> <summary><b>Endpoints & JSON</b></summary>

#### POST(GQL) request to post-service using:

```GraphQL query
postById(id: "post-1") {
   id
   text
   userId
   parentId
   created
} 
```

#

#### JSON response with given request above:

```JSON
{
  "data": {
    "postById": {
      "id": "post-1",
      "text": "text-1",
      "userId": "userId-1",
      "parentId": "parentId-1",
      "created": "YYYY-MM-DD"
    }
  }
}
```

</details>

------

## People who worked on this project:

###  * Oscar Eriksson Stjernfeldt

###  * Christian Löfqvist