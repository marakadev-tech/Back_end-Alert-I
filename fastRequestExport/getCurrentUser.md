# getCurrentUser

> URL: http://localhost:8086/auth/me
>
> Origin Url: http://localhost:8086/auth/me
>
> Type: GET


### Request headers

|Header Name| Header Value|
|---------|------|
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU1MTE0MTQzLCJleHAiOjE3NTUyMDA1NDN9.WmUt8PXJiCqVS97XKGwU1tlPzfdwbcxZU_0nYbkkhFA


### Parameters

##### Path parameters

| Parameter | Type | Value | Description |
|---------|------|------|------------|


##### URL parameters

|Required| Parameter | Type | Value | Description |
|---------|---------|------|------|------------|


##### Body parameters

###### JSON

```

```

###### JSON document

```
null
```


##### Form URL-Encoded
|Required| Parameter | Type | Value | Description |
|---------|---------|------|------|------------|


##### Multipart
|Required | Parameter | Type | Value | Description |
|---------|---------|------|------|------------|


### Response

##### Response example

```
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU1MTE0MTQzLCJleHAiOjE3NTUyMDA1NDN9.WmUt8PXJiCqVS97XKGwU1tlPzfdwbcxZU_0nYbkkhFA"
}
```

##### Response document
```
{
	"headers":{},
	"body":{
		"password":"No comment,Type =String",
		"role":"No comment,Type =String",
		"num_tel":"No comment,Type =Number",
		"localite":"No comment,Type =String",
		"id":"No comment,Type =Number",
		"prenom":"No comment,Type =String",
		"nom":"No comment,Type =String",
		"email":"No comment,Type =String"
	},
	"status":{}
}
```


