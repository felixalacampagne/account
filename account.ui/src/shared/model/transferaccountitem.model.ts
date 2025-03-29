import { TfrAccountItem } from "./tfraccountitem.model";

// transferaccountitem.model.ts
export class TransferAccountItem extends TfrAccountItem
{
public order : number = 0;
public relatedAccountName : string = '';
public type : string = '';
public token : string = '';
    constructor()
    {
        super();
    }
}
