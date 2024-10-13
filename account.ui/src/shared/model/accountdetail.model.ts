// accountdetail.model

// Actually extends AccountItem - is it worth figuring out how to do that???
export class AccountDetail
{
    public id : number = -1;
    public name : string = '';
    public address : string = '';
    public code : string = "";
    public currency : string = "";
    public format : string = "";
    public order : number = 0;
    public statement : string = "";
    public bic : string = "";
    public telephone : string = "";
    public token : string = "";
    public statementref : string = "";
    //constructor();
    //constructor(id : number, name: string);
    //constructor(id? : number, name?: string)
    constructor()
    {
       // this.id = id ?? -1;
       // this.name = name ?? '';
    }
}


